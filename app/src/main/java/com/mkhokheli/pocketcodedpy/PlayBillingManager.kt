package com.mkhokheli.pocketcodedpy

import android.app.Activity
import android.content.Context
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

internal data class BillingUiState(
    val connected: Boolean = false,
    val purchasesLoaded: Boolean = false,
    val isPremium: Boolean = false,
    val isPurchasePending: Boolean = false,
    val formattedPrice: String = DEFAULT_LIFETIME_PRICE,
    val message: String? = null,
)

internal class PlayBillingManager(
    context: Context,
    private val accountId: String,
    private val onStateChanged: (BillingUiState) -> Unit,
) : PurchasesUpdatedListener {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(BILLING_PREFS_NAME, Context.MODE_PRIVATE)
    private var state = BillingUiState(isPremium = cachedPremium())
    private var productDetails: ProductDetails? = null
    private var isClosed = false

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
                        message = billingMessage(result, "Google Play Billing is unavailable."),
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                if (!isClosed) update(connected = false)
            }
        })
    }

    fun launchPurchase(activity: Activity) {
        val details = productDetails
        if (!billingClient.isReady || details == null) {
            update(message = "The purchase is not ready yet. Check your connection and try again.")
            if (billingClient.isReady) queryProductDetails()
            return
        }
        val offerToken = details.oneTimePurchaseOfferDetailsList
            ?.firstOrNull()
            ?.offerToken
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
            .setObfuscatedAccountId(sha256(accountId))
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
            BillingClient.BillingResponseCode.OK -> processPurchases(purchases.orEmpty(), restoring = false)
            BillingClient.BillingResponseCode.USER_CANCELED -> update(message = "Purchase cancelled. No charge was made.")
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> queryPurchases(restoring = true)
            else -> update(message = billingMessage(result, "The purchase could not be completed."))
        }
    }

    private fun queryProductDetails() {
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
                update(message = billingMessage(result, "Could not load the purchase price."))
                return@queryProductDetailsAsync
            }
            productDetails = queryResult.productDetailsList.firstOrNull()
            val price = productDetails
                ?.oneTimePurchaseOfferDetailsList
                ?.firstOrNull()
                ?.formattedPrice
                ?: DEFAULT_LIFETIME_PRICE
            update(formattedPrice = price)
        }
    }

    private fun queryPurchases(restoring: Boolean = false) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (isClosed) return@queryPurchasesAsync
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases, restoring)
            } else {
                update(
                    purchasesLoaded = true,
                    message = billingMessage(result, "Could not check existing purchases."),
                )
            }
        }
    }

    private fun processPurchases(purchases: List<Purchase>, restoring: Boolean) {
        val matchingPurchases = purchases.filter { LIFETIME_PRODUCT_ID in it.products }
        val purchased = matchingPurchases.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        val pending = matchingPurchases.any { it.purchaseState == Purchase.PurchaseState.PENDING }

        if (purchased != null) {
            cachePremium(true)
            syncPremiumToFirestore(true)
            update(
                purchasesLoaded = true,
                isPremium = true,
                isPurchasePending = false,
                message = if (restoring) "Purchase restored. Premium is unlocked." else "Purchase complete. Premium is unlocked.",
            )
            if (!purchased.isAcknowledged) acknowledge(purchased)
            return
        }

        cachePremium(false)
        update(
            purchasesLoaded = true,
            isPremium = false,
            isPurchasePending = pending,
            message = when {
                pending -> "Your payment is pending. Premium unlocks after Google Play confirms it."
                restoring -> "No completed purchase was found for this Google Play account."
                else -> state.message
            },
        )
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
        isPurchasePending: Boolean = state.isPurchasePending,
        formattedPrice: String = state.formattedPrice,
        message: String? = state.message,
    ) {
        state = BillingUiState(
            connected = connected,
            purchasesLoaded = purchasesLoaded,
            isPremium = isPremium,
            isPurchasePending = isPurchasePending,
            formattedPrice = formattedPrice,
            message = message,
        )
        publish()
    }

    private fun publish() = onStateChanged(state)
}

private const val BILLING_PREFS_NAME = "play_billing"

private fun premiumKey(accountId: String): String = "premium_${sha256(accountId)}"

private fun sha256(value: String): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte) }
}

private fun billingMessage(result: BillingResult, fallback: String): String {
    return result.debugMessage.takeIf(String::isNotBlank) ?: fallback
}
