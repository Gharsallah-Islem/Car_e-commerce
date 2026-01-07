package com.example.carpartsecom.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.carpartsecom.MainActivity
import com.example.carpartsecom.R
import com.example.carpartsecom.ui.viewmodel.CheckoutViewModel
import com.example.carpartsecom.util.StripePaymentHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CheckoutFragment : Fragment() {
    private lateinit var checkoutViewModel: CheckoutViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_checkout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = requireActivity() as MainActivity
        val factory = mainActivity.viewModelFactory
        checkoutViewModel = ViewModelProvider(requireActivity(), factory)[CheckoutViewModel::class.java]

        // Set up Stripe payment callback
        mainActivity.setStripePaymentCallback { result ->
            view.post { handleStripePaymentResult(result) }
        }

        val addressLayout = view.findViewById<TextInputLayout>(R.id.addressLayout)
        val addressEdit = view.findViewById<TextInputEditText>(R.id.addressEdit)
        val phoneLayout = view.findViewById<TextInputLayout>(R.id.phoneLayout)
        val phoneEdit = view.findViewById<TextInputEditText>(R.id.phoneEdit)
        val notesLayout = view.findViewById<TextInputLayout>(R.id.notesLayout)
        val notesEdit = view.findViewById<TextInputEditText>(R.id.notesEdit)
        val latLayout = view.findViewById<TextInputLayout>(R.id.latLayout)
        val latEdit = view.findViewById<TextInputEditText>(R.id.latEdit)
        val lngLayout = view.findViewById<TextInputLayout>(R.id.lngLayout)
        val lngEdit = view.findViewById<TextInputEditText>(R.id.lngEdit)
        val paymentRadioGroup = view.findViewById<RadioGroup>(R.id.paymentRadioGroup)
        val placeOrderButton = view.findViewById<MaterialButton>(R.id.placeOrderButton)
        val selectOnMapButton = view.findViewById<MaterialButton>(R.id.selectOnMapButton)

        // Open map picker
        selectOnMapButton.setOnClickListener {
            val currentLat = latEdit.text.toString().toDoubleOrNull() ?: 36.8065
            val currentLng = lngEdit.text.toString().toDoubleOrNull() ?: 10.1815

            val mapFragment = MapPickerFragment.newInstance(
                initialLat = currentLat,
                initialLng = currentLng
            ) { lat, lng ->
                // Update via ViewModel so it persists across fragment recreation
                checkoutViewModel.setLocation(lat, lng)
            }
            mainActivity.navigateToFragment(mapFragment)
        }

        // Observe location changes from map picker
        checkoutViewModel.selectedLocation.observe(viewLifecycleOwner) { location ->
            location?.let { (lat, lng) ->
                latEdit.setText(String.format(java.util.Locale.US, "%.6f", lat))
                lngEdit.setText(String.format(java.util.Locale.US, "%.6f", lng))
            }
        }

        placeOrderButton.setOnClickListener {
            val address = addressEdit.text.toString().trim()
            val phone = phoneEdit.text.toString().trim()
            val notes = notesEdit.text.toString().trim()
            val latStr = latEdit.text.toString().trim()
            val lngStr = lngEdit.text.toString().trim()

            var isValid = true

            // Address validation
            if (address.isEmpty()) {
                addressLayout?.error = "Address is required"
                isValid = false
            } else if (address.length < 5) {
                addressLayout?.error = "Please enter a valid address"
                isValid = false
            } else {
                addressLayout?.error = null
            }

            // Phone validation - allow digits, spaces, +, -, ()
            val phoneDigits = phone.replace(Regex("[^0-9]"), "")
            if (phone.isEmpty()) {
                phoneLayout?.error = "Phone number is required"
                isValid = false
            } else if (phoneDigits.length < 8 || phoneDigits.length > 15) {
                phoneLayout?.error = "Please enter a valid phone number (8-15 digits)"
                isValid = false
            } else if (!phone.matches(Regex("^[+]?[0-9\\s\\-()]+$"))) {
                phoneLayout?.error = "Phone can only contain digits, +, -, (), and spaces"
                isValid = false
            } else {
                phoneLayout?.error = null
            }

            // Latitude validation
            val lat = latStr.toDoubleOrNull()
            if (latStr.isEmpty()) {
                latLayout?.error = "Latitude is required"
                isValid = false
            } else if (lat == null) {
                latLayout?.error = "Invalid number"
                isValid = false
            } else if (lat < -90 || lat > 90) {
                latLayout?.error = "Latitude must be between -90 and 90"
                isValid = false
            } else {
                latLayout?.error = null
            }

            // Longitude validation
            val lng = lngStr.toDoubleOrNull()
            if (lngStr.isEmpty()) {
                lngLayout?.error = "Longitude is required"
                isValid = false
            } else if (lng == null) {
                lngLayout?.error = "Invalid number"
                isValid = false
            } else if (lng < -180 || lng > 180) {
                lngLayout?.error = "Longitude must be between -180 and 180"
                isValid = false
            } else {
                lngLayout?.error = null
            }

            if (!isValid) return@setOnClickListener

            placeOrderButton.isEnabled = false
            placeOrderButton.text = "Placing Order..."
            
            checkoutViewModel.setLocation(lat ?: 0.0, lng ?: 0.0)
            checkoutViewModel.setDeliveryAddress(address)
            checkoutViewModel.setContactPhone(phone)
            checkoutViewModel.setDeliveryNotes(notes)
            
            val paymentMethod = if (paymentRadioGroup.checkedRadioButtonId == R.id.stripeRadio) "card" else "cash"
            checkoutViewModel.setPaymentMethod(paymentMethod)

            if (paymentMethod == "card") {
                // For card payments, create payment intent first
                placeOrderButton.text = "Setting up payment..."
                checkoutViewModel.createPaymentIntent()
            } else {
                // For cash on delivery, create order directly
                checkoutViewModel.createOrder()
            }
        }

        // Handle payment intent creation result
        checkoutViewModel.paymentIntentStatus.observe(viewLifecycleOwner) { result ->
            val button = view.findViewById<MaterialButton>(R.id.placeOrderButton)
            result.onSuccess { paymentIntent ->
                // Payment intent created - launch Stripe Payment Sheet via MainActivity
                mainActivity.stripePaymentHelper?.presentPaymentSheet(
                    clientSecret = paymentIntent.clientSecret,
                    paymentIntentId = paymentIntent.paymentIntentId,
                    merchantName = "Car Parts Store"
                ) ?: run {
                    button.isEnabled = true
                    button.text = "Place Order"
                    Snackbar.make(view, "Payment system not available", Snackbar.LENGTH_SHORT).show()
                }
            }
            result.onFailure {
                button.isEnabled = true
                button.text = "Place Order"
                Snackbar.make(view, "Payment setup failed: ${it.message}", Snackbar.LENGTH_SHORT).show()
            }
        }

        checkoutViewModel.createOrderStatus.observe(viewLifecycleOwner) { result ->
            val button = view.findViewById<MaterialButton>(R.id.placeOrderButton)
            button.isEnabled = true
            button.text = "Place Order"

            result.onSuccess { orderId ->
                Snackbar.make(view, "Order placed successfully! Order #$orderId", Snackbar.LENGTH_LONG).show()
                requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_orders
            }
            result.onFailure {
                Snackbar.make(view, "Order failed: ${it.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the callback when fragment is destroyed
        (activity as? MainActivity)?.setStripePaymentCallback(null)
    }

    private fun handleStripePaymentResult(result: StripePaymentHelper.PaymentResult) {
        val button = view?.findViewById<MaterialButton>(R.id.placeOrderButton)

        when (result) {
            is StripePaymentHelper.PaymentResult.Completed -> {
                // Payment successful - now create the order
                button?.text = "Creating order..."
                checkoutViewModel.createOrder(result.paymentIntentId)
            }
            is StripePaymentHelper.PaymentResult.Canceled -> {
                // User canceled payment
                button?.isEnabled = true
                button?.text = "Place Order"
                view?.let {
                    Snackbar.make(it, "Payment canceled", Snackbar.LENGTH_SHORT).show()
                }
            }
            is StripePaymentHelper.PaymentResult.Failed -> {
                // Payment failed
                button?.isEnabled = true
                button?.text = "Place Order"
                view?.let {
                    Snackbar.make(it, "Payment failed: ${result.error}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}
