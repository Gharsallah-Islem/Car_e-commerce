package com.example.carpartsecom.util

import android.content.Context
import androidx.activity.ComponentActivity
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

/**
 * Helper class to manage Stripe Payment Sheet
 * Must be created in Activity's onCreate before any fragments are attached
 */
class StripePaymentHelper(
    activity: ComponentActivity,
    private val onPaymentResult: (PaymentResult) -> Unit
) {
    private val paymentSheet: PaymentSheet
    private var currentPaymentIntentId: String? = null

    sealed class PaymentResult {
        data class Completed(val paymentIntentId: String) : PaymentResult()
        object Canceled : PaymentResult()
        data class Failed(val error: String) : PaymentResult()
    }

    init {
        // Initialize Payment Sheet - must be done in Activity onCreate
        paymentSheet = PaymentSheet(activity) { result ->
            handlePaymentResult(result)
        }
    }

    /**
     * Present the payment sheet to collect payment
     * @param clientSecret The client secret from the PaymentIntent
     * @param paymentIntentId The PaymentIntent ID for reference
     * @param merchantName The name to display on the payment sheet
     */
    fun presentPaymentSheet(
        clientSecret: String,
        paymentIntentId: String,
        merchantName: String = "Car Parts Store"
    ) {
        currentPaymentIntentId = paymentIntentId

        val configuration = PaymentSheet.Configuration(
            merchantDisplayName = merchantName
        )

        paymentSheet.presentWithPaymentIntent(
            clientSecret,
            configuration
        )
    }

    private fun handlePaymentResult(result: PaymentSheetResult) {
        when (result) {
            is PaymentSheetResult.Completed -> {
                currentPaymentIntentId?.let {
                    onPaymentResult(PaymentResult.Completed(it))
                } ?: onPaymentResult(PaymentResult.Failed("Payment intent ID not found"))
            }
            is PaymentSheetResult.Canceled -> {
                onPaymentResult(PaymentResult.Canceled)
            }
            is PaymentSheetResult.Failed -> {
                onPaymentResult(PaymentResult.Failed(result.error.localizedMessage ?: "Payment failed"))
            }
        }
    }

    companion object {
        /**
         * Initialize Stripe SDK - call in Application or MainActivity onCreate
         */
        fun initialize(context: Context) {
            PaymentConfiguration.init(
                context.applicationContext,
                Constants.STRIPE_PUBLISHABLE_KEY
            )
        }
    }
}

