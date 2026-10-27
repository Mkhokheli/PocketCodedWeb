package com.mkhokheli.pocketcodedweb

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
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
import com.google.android.gms.tasks.Tasks
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.auth.FirebaseAuth
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.Executors
import org.json.JSONObject

internal const val LIFETIME_PRODUCT_ID = "pocketcodedweb_lifetime"
internal const val MONTHLY_PRODUCT_ID = "pocketcodedweb_premium_monthly"
internal const val MONTHLY_BASE_PLAN_ID = "monthly"
private const val BILLING_PREFS_NAME = "play_billing"
private const val PURCHASE_ACCOUNT_GRACE_MS = 24L * 60L * 60L * 1000L
private const val BILLING_LOG_TAG = "PlayBilling"
private const val VERIFY_PURCHASE_URL =
    "https://us-central1-pocketcodedweb-6887b.cloudfunctions.net/verifyPlayPurchase"

internal data class BillingUiState(
    val connected: Boolean = false,
    val purchasesLoaded: Boolean = false,
    val isPremium: Boolean = false,
    val premiumVerified: Boolean = false,
    val isPurchasePending: Boolean = false,
    val productDetailsLoaded: Boolean = false,
    val productAvailable: Boolean = false,
    val formattedPrice: String = "",
    val monthlyPrice: String = "",
    val monthlyAvailable: Boolean = false,
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
            return prefs.getBoolean(premiumKey(accountId), false) ||
                prefs.getBoolean(legacyPremiumKey(accountId), false)
        }

        fun clearBillingCacheForUser(context: Context, accountId: String) {
            val prefs = context.getSharedPreferences(BILLING_PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .remove(premiumKey(accountId))
                .remove(legacyPremiumKey(accountId))
                .apply()
        }

        fun updateLocalPremiumFromServer(context: Context, accountId: String, isPremium: Boolean) {
            val prefs = context.getSharedPreferences(BILLING_PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(premiumKey(accountId), isPremium)
                .remove(legacyPremiumKey(accountId))
                .apply()
        }
    }

    private val appContext = context.applicationContext
    private val expectedObfuscatedAccountId = sha256(accountId)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val preferences = appContext.getSharedPreferences(BILLING_PREFS_NAME, Context.MODE_PRIVATE)
    private var state = BillingUiState(isPremium = cachedPremium())
    private var productDetails: ProductDetails? = null
    private var monthlyDetails: ProductDetails? = null
    private var serverEntitlementKnown = cachedPremium()
    private var serverPremium = cachedPremium()
    private var isClosed = false
    @Volatile private var purchaseTokenBeingVerified: String? = null
    private val verificationExecutor = Executors.newSingleThreadExecutor()

    private val billingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    fun start() {
        publish()
        billingClient.startConnection(object : BillingClientStateListener {
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
                        isPremium = cachedPremium(),
                        premiumVerified = false,
                        message = billingMessage(result, "Google Play Billing is unavailable."),
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                if (!isClosed) update(connected = false)
            }
        })
    }

    fun launchPurchase(activity: Activity, monthly: Boolean = false) {
        val details = if (monthly) monthlyDetails else productDetails
        if (!billingClient.isReady || details == null) {
            update(message = "The purchase is not ready yet. Check your connection and try again.")
            if (billingClient.isReady) queryProductDetails()
            return
        }
        val offerToken = if (monthly) monthlyOffer(details)?.offerToken
            else details.oneTimePurchaseOfferDetailsList?.firstOrNull()?.offerToken
        if (offerToken.isNullOrBlank()) {
            update(message = "This purchase is not available for your Google Play account or region.")
            return
        }
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
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

    fun refreshPurchases() {
        if (billingClient.isReady) queryPurchases()
    }

    fun applyServerEntitlement(isPremium: Boolean) {
        if (isClosed) return
        serverEntitlementKnown = true
        serverPremium = isPremium
        cachePremium(isPremium)
        update(
            purchasesLoaded = true,
            isPremium = isPremium,
            premiumVerified = true,
            isPurchasePending = false,
            message = if (isPremium) null else state.message,
        )
    }

    fun close() {
        isClosed = true
        billingClient.endConnection()
        verificationExecutor.shutdownNow()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (isClosed) return
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> processPurchases(purchases.orEmpty(), restoring = false)
            BillingClient.BillingResponseCode.USER_CANCELED -> update(message = "Purchase cancelled. No charge was made.")
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> queryPurchases(restoring = true)
            else -> update(message = billingMessage(result, "The purchase could not be completed."))
        }
    }

    private fun monthlyOffer(details: ProductDetails) = details.subscriptionOfferDetails?.firstOrNull {
        it.basePlanId == MONTHLY_BASE_PLAN_ID && it.offerId == null &&
            it.pricingPhases.pricingPhaseList.any { phase -> phase.billingPeriod == "P1M" }
    }

    private fun queryMonthlyDetails() {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(MONTHLY_PRODUCT_ID).setProductType(BillingClient.ProductType.SUBS).build()
        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()
        ) { result, response ->
            if (isClosed) return@queryProductDetailsAsync
            monthlyDetails = if (result.responseCode == BillingClient.BillingResponseCode.OK)
                response.productDetailsList.firstOrNull { it.productId == MONTHLY_PRODUCT_ID } else null
            val offer = monthlyDetails?.let { monthlyOffer(it) }
            val price = offer?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice.orEmpty()
            update(monthlyPrice = price, monthlyAvailable = offer != null && price.isNotBlank())
        }
    }

    private fun queryProductDetails() {
        queryMonthlyDetails()
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(LIFETIME_PRODUCT_ID)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()
        billingClient.queryProductDetailsAsync(params) { result, queryResult ->
            if (isClosed) return@queryProductDetailsAsync
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                productDetails = null
                update(
                    productDetailsLoaded = true,
                    productAvailable = false,
                    formattedPrice = "",
                    message = billingMessage(result, "Could not load the purchase price."),
                )
                return@queryProductDetailsAsync
            }
            productDetails = queryResult.productDetailsList.firstOrNull {
                it.productId == LIFETIME_PRODUCT_ID
            }
            val price = productDetails
                ?.oneTimePurchaseOfferDetailsList
                ?.map { it.formattedPrice }
                ?.let(::localizedGooglePlayPrice)
                .orEmpty()
            val productIsAvailable = productDetails != null && price.isNotBlank()
            if (!productIsAvailable) productDetails = null
            update(
                productDetailsLoaded = true,
                productAvailable = productIsAvailable,
                formattedPrice = price,
                message = if (productIsAvailable) {
                    state.message
                } else {
                    "Premium is not available yet. Install from the Play Console internal test link and activate $LIFETIME_PRODUCT_ID."
                },
            )
        }
    }

    private fun queryPurchases(restoring: Boolean = false) {
        val collected = mutableListOf<Purchase>()
        var remaining = 2
        var failed = false
        for (type in listOf(BillingClient.ProductType.INAPP, BillingClient.ProductType.SUBS)) {
            val params = QueryPurchasesParams.newBuilder().setProductType(type).build()
            billingClient.queryPurchasesAsync(params) { result, purchases ->
                if (isClosed) return@queryPurchasesAsync
                if (result.responseCode == BillingClient.BillingResponseCode.OK) collected.addAll(purchases)
                else failed = true
                remaining--
                if (remaining == 0) {
                    if (!failed) processPurchases(collected, restoring)
                    else update(purchasesLoaded = true, premiumVerified = false,
                        message = "Could not check all purchases. Please try Restore Purchase again.")
                }
            }
        }
    }

    private fun processPurchases(purchases: List<Purchase>, restoring: Boolean) {
        Log.d(BILLING_LOG_TAG, "Checking ${purchases.size} purchases for $LIFETIME_PRODUCT_ID.")
        val matchingPurchases = purchases.filter { LIFETIME_PRODUCT_ID in it.products || MONTHLY_PRODUCT_ID in it.products }.sortedBy { if (LIFETIME_PRODUCT_ID in it.products) 0 else 1 }
        val purchased = matchingPurchases.firstOrNull {
            it.purchaseState == Purchase.PurchaseState.PURCHASED && isValidPurchase(it)
        }
        val pending = matchingPurchases.any { it.purchaseState == Purchase.PurchaseState.PENDING }

        if (purchased != null) {
            verifyPurchaseWithBackend(purchased, restoring)
            return
        }

        val retainedServerPremium = serverEntitlementKnown && serverPremium
        cachePremium(retainedServerPremium)
        update(
            purchasesLoaded = true,
            isPremium = retainedServerPremium,
            premiumVerified = serverEntitlementKnown,
            isPurchasePending = pending,
            message = when {
                retainedServerPremium -> null
                pending -> "Your payment is pending. Premium unlocks after Google Play confirms it."
                restoring -> "No completed purchase was found for this Google Play account."
                else -> state.message
            },
        )
    }

    private fun unlockPremium(purchase: Purchase, restoring: Boolean) {
        serverEntitlementKnown = true
        serverPremium = true
        cachePremium(true)
        update(
            purchasesLoaded = true,
            isPremium = true,
            premiumVerified = true,
            isPurchasePending = false,
            message = if (restoring) {
                "Purchase restored. Premium is unlocked."
            } else {
                "Purchase complete. Premium is unlocked."
            },
        )
        if (!purchase.isAcknowledged) acknowledge(purchase)
    }

    private fun verifyPurchaseWithBackend(purchase: Purchase, restoring: Boolean) {
        if (purchaseTokenBeingVerified == purchase.purchaseToken) return
        purchaseTokenBeingVerified = purchase.purchaseToken
        update(
            purchasesLoaded = false,
            isPurchasePending = false,
            message = "Verifying your purchase securely…",
        )
        verificationExecutor.execute {
            val verification = runCatching {
                val user = FirebaseAuth.getInstance().currentUser
                    ?: error("Sign in before verifying your purchase.")
                if (user.uid != accountId) error("Your signed-in account changed during verification.")
                val idToken = Tasks.await(user.getIdToken(true)).token
                    ?: error("Could not verify your sign-in session.")
                val appCheckToken = Tasks.await(
                    FirebaseAppCheck.getInstance().getAppCheckToken(false)
                ).token
                if (appCheckToken.isBlank()) error("Could not verify this app installation.")
                val payload = JSONObject()
                    .put("productId", if (LIFETIME_PRODUCT_ID in purchase.products) LIFETIME_PRODUCT_ID else MONTHLY_PRODUCT_ID)
                    .put("purchaseToken", purchase.purchaseToken)
                    .toString()
                val connection = (URL(VERIFY_PURCHASE_URL).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 20_000
                    readTimeout = 30_000
                    doOutput = true
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Authorization", "Bearer $idToken")
                    setRequestProperty("X-Firebase-AppCheck", appCheckToken)
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                }
                try {
                    connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
                    val status = connection.responseCode
                    val stream = if (status in 200..299) connection.inputStream else connection.errorStream
                    val responseText = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                    val json = runCatching { JSONObject(responseText) }.getOrDefault(JSONObject())
                    if (status !in 200..299) {
                        error(json.optString("error").ifBlank { "Purchase verification failed (HTTP $status)." })
                    }
                    if (!json.optBoolean("verified") || !json.optBoolean("active")) {
                        error("Google Play says this purchase is not active.")
                    }
                } finally {
                    connection.disconnect()
                }
            }
            mainHandler.post {
                if (isClosed) return@post
                purchaseTokenBeingVerified = null
                verification.onSuccess { unlockPremium(purchase, restoring) }
                    .onFailure { error ->
                        val retainedServerPremium = serverEntitlementKnown && serverPremium
                        cachePremium(retainedServerPremium)
                        update(
                            purchasesLoaded = true,
                            isPremium = retainedServerPremium,
                            premiumVerified = serverEntitlementKnown,
                            message = error.message ?: "Could not verify this purchase securely.",
                        )
                    }
            }
        }
    }

    private fun isValidPurchase(purchase: Purchase): Boolean {
        val orderId = purchase.orderId
        val purchaseAccountId = purchase.accountIdentifiers?.obfuscatedAccountId

        if (!isGooglePlayOrderId(orderId)) {
            Log.w(BILLING_LOG_TAG, "Purchase rejected: invalid or missing Google Play order ID: $orderId")
            return false
        }
        if (!purchaseAccountMatchesExpected(expectedObfuscatedAccountId, purchaseAccountId)) {
            Log.w(BILLING_LOG_TAG, "Purchase rejected: owned by a different signed-in app account.")
            return false
        }
        if (!purchaseTimeFitsAccount(purchase.purchaseTime, trialStartTime)) {
            Log.w(BILLING_LOG_TAG, "Purchase rejected: purchase predates this app account.")
            return false
        }

        return true
    }

    private fun acknowledge(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { result ->
            if (!isClosed && result.responseCode != BillingClient.BillingResponseCode.OK) {
                update(message = billingMessage(result, "Premium is unlocked, but purchase confirmation will retry later."))
            }
        }
    }

    private fun cachedPremium(): Boolean =
        preferences.getBoolean(premiumKey(accountId), false) ||
            preferences.getBoolean(legacyPremiumKey(accountId), false)

    private fun cachePremium(value: Boolean) {
        preferences.edit()
            .putBoolean(premiumKey(accountId), value)
            .remove(legacyPremiumKey(accountId))
            .apply()
    }

    private fun update(
        connected: Boolean = state.connected,
        purchasesLoaded: Boolean = state.purchasesLoaded,
        isPremium: Boolean = state.isPremium,
        premiumVerified: Boolean = state.premiumVerified,
        isPurchasePending: Boolean = state.isPurchasePending,
        productDetailsLoaded: Boolean = state.productDetailsLoaded,
        productAvailable: Boolean = state.productAvailable,
        formattedPrice: String = state.formattedPrice,
        monthlyPrice: String = state.monthlyPrice,
        monthlyAvailable: Boolean = state.monthlyAvailable,
        message: String? = state.message,
    ) {
        state = BillingUiState(
            connected = connected,
            purchasesLoaded = purchasesLoaded,
            isPremium = isPremium,
            premiumVerified = premiumVerified,
            isPurchasePending = isPurchasePending,
            productDetailsLoaded = productDetailsLoaded,
            productAvailable = productAvailable,
            formattedPrice = formattedPrice,
            monthlyPrice = monthlyPrice,
            monthlyAvailable = monthlyAvailable,
            message = message,
        )
        publish()
    }

    private fun publish() {
        val snapshot = state
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onStateChanged(snapshot)
        } else {
            mainHandler.post {
                if (!isClosed) onStateChanged(snapshot)
            }
        }
    }
}

internal fun isGooglePlayOrderId(orderId: String?): Boolean =
    !orderId.isNullOrBlank() && orderId.startsWith("GPA.")

internal fun purchaseAccountMatchesExpected(
    expectedObfuscatedAccountId: String,
    actualObfuscatedAccountId: String?,
): Boolean = actualObfuscatedAccountId == expectedObfuscatedAccountId

internal fun purchaseTimeFitsAccount(purchaseTime: Long, trialStartTime: Long): Boolean {
    return purchaseTime > 0L && purchaseTime >= trialStartTime - PURCHASE_ACCOUNT_GRACE_MS
}

internal fun localizedGooglePlayPrice(formattedPrices: List<String>): String =
    formattedPrices.firstOrNull { it.isNotBlank() }.orEmpty()

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
            "Google Play says this build is not configured for billing. Install PocketCodedWeb from the Play Console internal test link, make sure this version is uploaded for com.mkhokheli.pocketcodedweb, and activate the pocketcodedweb_lifetime one-time product."
        BillingClient.BillingResponseCode.DEVELOPER_ERROR ->
            "Google Play rejected the billing setup. Check that the package name, signing key, uploaded version, and product ID pocketcodedweb_lifetime match Play Console."
        BillingClient.BillingResponseCode.ITEM_UNAVAILABLE ->
            "The Premium product is not active or available for this tester, country, or Play track."
        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE ->
            "Google Play Billing is temporarily unavailable. Check the Play Store connection and try again."
        else -> debugMessage ?: fallback
    }
}
