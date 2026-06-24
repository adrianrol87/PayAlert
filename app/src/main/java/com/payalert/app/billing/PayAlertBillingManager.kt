package com.payalert.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.payalert.app.data.Monetization
import com.payalert.app.data.ProAccessRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProBillingUiState(
    val priceLabel: String = Monetization.proPriceLabel,
    val isReady: Boolean = false,
    val isBusy: Boolean = false,
    val message: String? = null,
)

class PayAlertBillingManager(
    private val context: Context,
    private val proAccessRepository: ProAccessRepository,
) : PurchasesUpdatedListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val _uiState = MutableStateFlow(ProBillingUiState())
    val uiState: StateFlow<ProBillingUiState> = _uiState.asStateFlow()

    private var productDetails: ProductDetails? = null

    fun start() {
        if (billingClient.isReady) {
            queryProduct()
            refreshPurchases()
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProduct()
                    refreshPurchases()
                } else {
                    showMessage("No se pudo iniciar Google Play Billing.")
                }
            }

            override fun onBillingServiceDisconnected() {
                _uiState.update { current -> current.copy(isReady = false) }
            }
        })
    }

    fun launchPurchase(activity: Activity) {
        val details = productDetails
        if (!billingClient.isReady || details == null) {
            showMessage("El producto Pro aun no esta disponible.")
            return
        }

        _uiState.update { current -> current.copy(isBusy = true, message = null) }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build(),
                ),
            )
            .build()

        val result = billingClient.launchBillingFlow(activity, billingFlowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _uiState.update { current -> current.copy(isBusy = false) }
            showMessage("No se pudo abrir la compra de Google Play.")
        }
    }

    fun restorePurchases() {
        refreshPurchases(showRestoreMessage = true)
    }

    fun refreshPurchases(showRestoreMessage: Boolean = false) {
        if (!billingClient.isReady) {
            start()
            return
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPro = purchases
                    .filter { purchase -> purchase.products.contains(Monetization.proProductId) }
                    .any { purchase -> purchase.purchaseState == Purchase.PurchaseState.PURCHASED }

                scope.launch {
                    proAccessRepository.setProUnlocked(hasPro)
                }

                purchases.forEach(::processPurchase)

                if (showRestoreMessage) {
                    val message = if (hasPro) {
                        "Compra restaurada correctamente."
                    } else {
                        "No encontramos una compra activa de PayAlert Pro."
                    }
                    showMessage(message)
                }
            } else if (showRestoreMessage) {
                showMessage("No se pudo restaurar la compra.")
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        _uiState.update { current -> current.copy(isBusy = false) }

        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases.orEmpty().forEach(::processPurchase)
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                showMessage("Compra cancelada.")
            }
            else -> {
                showMessage("Google Play no pudo completar la compra.")
            }
        }
    }

    fun dispose() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    private fun queryProduct() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(Monetization.proProductId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                showMessage("No se pudo cargar PayAlert Pro desde Google Play.")
                return@queryProductDetailsAsync
            }

            productDetails = productDetailsList.firstOrNull()
            val localizedPrice = productDetails
                ?.oneTimePurchaseOfferDetails
                ?.formattedPrice
                ?: Monetization.proPriceLabel

            _uiState.update { current ->
                current.copy(
                    priceLabel = localizedPrice,
                    isReady = productDetails != null,
                    message = current.message,
                )
            }
        }
    }

    private fun processPurchase(purchase: Purchase) {
        if (!purchase.products.contains(Monetization.proProductId)) return
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        scope.launch {
            proAccessRepository.setProUnlocked(true)
        }

        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    showMessage("PayAlert Pro activado.")
                }
            }
        } else {
            showMessage("PayAlert Pro activado.")
        }
    }

    private fun showMessage(message: String) {
        _uiState.update { current ->
            current.copy(message = message, isBusy = false)
        }
    }
}
