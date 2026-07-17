package com.mkhokheli.pocketcodedpy

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.security.MessageDigest

internal const val LIFETIME_PRODUCT_ID = "pocketcodedpy_lifetime"
internal const val DEFAULT_LIFETIME_PRICE = "R99.99"
private const val PURCHASE_ACCOUNT_GRACE_MS = 24L * 60L * 60L * 1000L

internal data class BillingUiState(
        val connected: Boolean = false,
        val purchasesLoaded: Boolean = false,
        val isPremium: Boolean = false,
        val premiumVerified: Boolean = false,
        val isPurchasePending: Boolean = false,
        val formattedPrice: String = DEFAULT_LIFETIME_PRICE,
        val message: String? = null,
)

internal class PlayBillingManager(
        context: Context,
        private val accountId: String,
        private val trialStartTime: Long = System.currentTimeMillis(),
        private val onStateChanged: (BillingUiState) -> Unit,
) : PurchasesUpdatedListener {
    companion object {
        fun isLocalPremium(context: Context, accountId: String): Boolean {
            val prefs = context.getSharedPreferences(BILLING_PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(premiumKey(accountId), false)
        }

        fun clearBillingCacheForUser(context: Context, accountId: String) {
            Log.d("PlayBilling", "Clearing billing cache for user: $accountId")
            val prefs = context.getSharedPreferences(BILLING_PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                    .remove(premiumKey(accountId))
                    .remove(legacyPremiumKey(accountId))
                    .apply()
        }
    }

    private val appContext = context.applicationContext
    private val expectedObfuscatedAccountId = sha256(accountId)
    private val preferences =
            appContext.getSharedPreferences(BILLING_PREFS_NAME, Context.MODE_PRIVATE)
    private var state = BillingUiState(isPremium = cachedPremium())
    private var productDetails: ProductDetails? = null
    private var isClosed = false

    private val billingClient =
            BillingClient.newBuilder(appContext)
                    .setListener(this)
                    .enablePendingPurchases(
                            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
                    )
                    .enableAutoServiceReconnection()
                    .build()

    fun start() {
        publish()
        billingClient.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        if (isClosed) return
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            update(connected = true, message = null)
                            queryProductDetails()
                            queryPurchases()
                        } else {
                            update(
                                    connected = false,
                                    purchasesLoaded = true,
                                    message =
                                            billingMessage(
                                                    result,
                                                    "Google Play Billing is unavailable."
                                            ),
                            )
                        }
                    }

                    override fun onBillingServiceDisconnected() {
                        if (!isClosed) update(connected = false)
                    }
                }
        )
    }

    fun launchPurchase(activity: Activity) {
        val details = productDetails
        if (!billingClient.isReady || details == null) {
            update(message = "The purchase is not ready yet. Check your connection and try again.")
            if (billingClient.isReady) queryProductDetails()
            return
        }
        val offerToken = details.oneTimePurchaseOfferDetailsList?.firstOrNull()?.offerToken
        if (offerToken.isNullOrBlank()) {
            update(
                    message =
                            "This purchase is not available for your Google Play account or region."
            )
            return
        }
        val productParams =
                BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(offerToken)
                        .build()
        val flowParams =
                BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(listOf(productParams))
                        .setObfuscatedAccountId(expectedObfuscatedAccountId)
                        .build()
        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            update(message = billingMessage(result, "Could not open Google Play checkout."))
        }
    }

    fun restorePurchases() {
        if (!billingClient.isReady) {
            update(message = "Connecting to Google Play. Try Restore Purchase again in a moment.")
            return
        }
        update(message = "Checking Google Play for your purchase...")
        queryPurchases(restoring = true)
    }

    fun close() {
        isClosed = true
        billingClient.endConnection()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (isClosed) return
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK ->
                    processPurchases(purchases.orEmpty(), restoring = false)
            BillingClient.BillingResponseCode.USER_CANCELED ->
                    update(message = "Purchase cancelled. No charge was made.")
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> queryPurchases(restoring = true)
            else -> update(message = billingMessage(result, "The purchase could not be completed."))
        }
    }

    private fun queryProductDetails() {
        val product =
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(LIFETIME_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
        val params = QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()
        billingClient.queryProductDetailsAsync(params) { result, queryResult ->
            if (isClosed) return@queryProductDetailsAsync
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                update(message = billingMessage(result, "Could not load the purchase price."))
                return@queryProductDetailsAsync
            }
            productDetails = queryResult.productDetailsList.firstOrNull()
            val price =
                    productDetails?.oneTimePurchaseOfferDetailsList?.firstOrNull()?.formattedPrice
                            ?: DEFAULT_LIFETIME_PRICE
            update(formattedPrice = price)
        }
    }

    private fun queryPurchases(restoring: Boolean = false) {
        val params =
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (isClosed) return@queryPurchasesAsync
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases, restoring)
            } else {
                update(
                        purchasesLoaded = true,
                        isPremium = cachedPremium(),
                        premiumVerified = false,
                        isPurchasePending = false,
                        message = billingMessage(result, "Could not check existing purchases."),
                )
            }
        }
    }

    private fun processPurchases(purchases: List<Purchase>, restoring: Boolean) {
        Log.d(
                "PlayBilling",
                "processPurchases() called with ${purchases.size} purchases, restoring=$restoring"
        )
        for (p in purchases) {
            Log.d(
                    "PlayBilling",
                    "  Purchase: productId=${p.products}, state=${p.purchaseState}, orderId=${p.orderId}, purchaseTime=${p.purchaseTime}, accountId=${p.accountIdentifiers?.obfuscatedAccountId}, token=${p.purchaseToken.take(20)}..."
            )
        }

        val matchingPurchases = purchases.filter { LIFETIME_PRODUCT_ID in it.products }
        Log.d("PlayBilling", "  Matching LIFETIME_PRODUCT_ID: ${matchingPurchases.size} purchases")

        val purchased =
                matchingPurchases.firstOrNull {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED && isValidPurchase(it)
                }
        val pending = matchingPurchases.any { it.purchaseState == Purchase.PurchaseState.PENDING }

        if (purchased != null) {
            Log.d(
                    "PlayBilling",
                    "  Valid account-bound purchase found. purchaseTime=${purchased.purchaseTime}ms, setting isPremium=true"
            )
            cachePremium(true)
            syncPremiumToFirestore(true)
            update(
                    purchasesLoaded = true,
                    isPremium = true,
                    premiumVerified = true,
                    isPurchasePending = false,
                    message =
                            if (restoring) "Purchase restored. Premium is unlocked."
                            else "Purchase complete. Premium is unlocked.",
            )
            if (!purchased.isAcknowledged) acknowledge(purchased)
            return
        }

        Log.d("PlayBilling", "  No valid account-bound purchase found. Setting isPremium=false")
        cachePremium(false)
        syncPremiumToFirestore(false)
        update(
                purchasesLoaded = true,
                isPremium = false,
                premiumVerified = false,
                isPurchasePending = pending,
                message =
                        when {
                            pending ->
                                    "Your payment is pending. Premium unlocks after Google Play confirms it."
                            restoring ->
                                    "No completed purchase was found for this Google Play account."
                            else -> state.message
                        },
        )
    }

    private fun isValidPurchase(purchase: Purchase): Boolean {
        val purchaseTime = purchase.purchaseTime
        val orderId = purchase.orderId
        val purchaseAccountId = purchase.accountIdentifiers?.obfuscatedAccountId

        if (!isGooglePlayOrderId(orderId)) {
            Log.w(
                    "PlayBilling",
                    "  Purchase rejected: invalid or missing Google Play order ID: $orderId"
            )
            return false
        }

        if (!purchaseAccountMatchesExpected(expectedObfuscatedAccountId, purchaseAccountId)) {
            Log.w(
                    "PlayBilling",
                    "  Purchase rejected: owned by a different app account. expected=$expectedObfuscatedAccountId actual=$purchaseAccountId"
            )
            return false
        }

        if (!purchaseTimeFitsAccount(purchaseTime, trialStartTime)) {
            Log.w(
                    "PlayBilling",
                    "  Purchase rejected: purchaseTime=$purchaseTime is before account trialStartTime=$trialStartTime"
            )
            return false
        }

        Log.d("PlayBilling", "  Purchase validation passed for current app account.")
        return true
    }

    private fun acknowledge(purchase: Purchase) {
        val params =
                AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
        billingClient.acknowledgePurchase(params) { result ->
            if (!isClosed && result.responseCode != BillingClient.BillingResponseCode.OK) {
                update(
                        message =
                                billingMessage(
                                        result,
                                        "Premium is unlocked, but purchase confirmation will retry later."
                                )
                )
            }
        }
    }

    private fun syncPremiumToFirestore(isPremium: Boolean) {
        if (accountId.isBlank()) return
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(accountId)
                .set(mapOf("isPremium" to isPremium), SetOptions.merge())
    }

    private fun cachedPremium(): Boolean = preferences.getBoolean(premiumKey(accountId), false)

    private fun cachePremium(value: Boolean) {
        preferences.edit().putBoolean(premiumKey(accountId), value).apply()
    }

    private fun update(
            connected: Boolean = state.connected,
            purchasesLoaded: Boolean = state.purchasesLoaded,
            isPremium: Boolean = state.isPremium,
            premiumVerified: Boolean = state.premiumVerified,
            isPurchasePending: Boolean = state.isPurchasePending,
            formattedPrice: String = state.formattedPrice,
            message: String? = state.message,
    ) {
        state =
                BillingUiState(
                        connected = connected,
                        purchasesLoaded = purchasesLoaded,
                        isPremium = isPremium,
                        premiumVerified = premiumVerified,
                        isPurchasePending = isPurchasePending,
                        formattedPrice = formattedPrice,
                        message = message,
                )
        publish()
    }

    private fun publish() = onStateChanged(state)
}

private const val BILLING_PREFS_NAME = "play_billing"

internal fun isGooglePlayOrderId(orderId: String?): Boolean =
        !orderId.isNullOrBlank() && orderId.startsWith("GPA.")

internal fun purchaseAccountMatchesExpected(
        expectedObfuscatedAccountId: String,
        actualObfuscatedAccountId: String?,
): Boolean = actualObfuscatedAccountId == expectedObfuscatedAccountId

internal fun purchaseTimeFitsAccount(purchaseTime: Long, trialStartTime: Long): Boolean {
    return purchaseTime > 0L && purchaseTime >= trialStartTime - PURCHASE_ACCOUNT_GRACE_MS
}

private fun premiumKey(accountId: String): String = "premium_v2_${sha256(accountId)}"

private fun legacyPremiumKey(accountId: String): String = "premium_${sha256(accountId)}"

private fun sha256(value: String): String {
    return MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }
}

private fun billingMessage(result: BillingResult, fallback: String): String {
    val debugMessage = result.debugMessage.takeIf(String::isNotBlank)
    return when (result.responseCode) {
        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
                "Google Play says this build is not configured for billing. Install the app from the Play Console internal test link, make sure this version is uploaded for com.mkhokheli.pocketcodedpy, and activate the pocketcodedpy_lifetime one-time product."
        BillingClient.BillingResponseCode.DEVELOPER_ERROR ->
                "Google Play rejected the billing setup. Check that the package name, signing key, uploaded version, and product ID pocketcodedpy_lifetime match Play Console."
        BillingClient.BillingResponseCode.ITEM_UNAVAILABLE ->
                "The Premium product is not active or available for this tester, country, or Play track."
        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE ->
                "Google Play Billing is temporarily unavailable. Check the Play Store connection and try again."
        else -> debugMessage ?: fallback
    }
}
