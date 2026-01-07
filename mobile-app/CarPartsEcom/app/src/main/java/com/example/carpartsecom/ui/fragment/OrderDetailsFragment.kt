package com.example.carpartsecom.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carpartsecom.MainActivity
import com.example.carpartsecom.R
import com.example.carpartsecom.ui.adapter.OrderItemAdapter
import com.example.carpartsecom.ui.adapter.OrderItemDisplay
import com.example.carpartsecom.ui.viewmodel.OrderViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailsFragment : Fragment() {
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var adapter: OrderItemAdapter
    private var orderId: Long = -1

    companion object {
        private const val ARG_ORDER_ID = "order_id"

        fun newInstance(orderId: Long): OrderDetailsFragment {
            val fragment = OrderDetailsFragment()
            val args = Bundle()
            args.putLong(ARG_ORDER_ID, orderId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orderId = arguments?.getLong(ARG_ORDER_ID) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = (requireActivity() as MainActivity).viewModelFactory
        orderViewModel = ViewModelProvider(requireActivity(), factory)[OrderViewModel::class.java]

        // Setup Toolbar
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        // Setup RecyclerView for order items
        val recyclerView = view.findViewById<RecyclerView>(R.id.orderItemsRecyclerView)
        adapter = OrderItemAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Views
        val orderIdText = view.findViewById<TextView>(R.id.orderId)
        val orderStatusText = view.findViewById<TextView>(R.id.orderStatus)
        val orderDateText = view.findViewById<TextView>(R.id.orderDate)
        val orderTotalText = view.findViewById<TextView>(R.id.orderTotal)
        val paymentMethodText = view.findViewById<TextView>(R.id.paymentMethod)
        val paymentIdText = view.findViewById<TextView>(R.id.paymentId)
        val paymentIdLayout = view.findViewById<LinearLayout>(R.id.paymentIdLayout)
        val deliveryLocationText = view.findViewById<TextView>(R.id.deliveryLocation)
        val deliveryAddressText = view.findViewById<TextView>(R.id.deliveryAddress)
        val addressLayout = view.findViewById<LinearLayout>(R.id.addressLayout)
        val contactPhoneText = view.findViewById<TextView>(R.id.contactPhone)
        val phoneLayout = view.findViewById<LinearLayout>(R.id.phoneLayout)
        val deliveryNotesText = view.findViewById<TextView>(R.id.deliveryNotes)
        val notesLayout = view.findViewById<LinearLayout>(R.id.notesLayout)
        val cancelButton = view.findViewById<MaterialButton>(R.id.cancelButton)
        val paymentCard = view.findViewById<MaterialCardView>(R.id.paymentCard)
        val deliveryCard = view.findViewById<MaterialCardView>(R.id.deliveryCard)

        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US)

        // Store order total for fallback display
        var orderTotal = 0.0
        var isOrderCancelled = false

        // Observe order details
        orderViewModel.getOrderById(orderId).observe(viewLifecycleOwner) { order ->
            if (order == null) {
                // Don't crash - just show a message if we haven't already handled this
                if (!isOrderCancelled) {
                    Toast.makeText(context, "Order not found", Toast.LENGTH_SHORT).show()
                    try {
                        requireActivity().onBackPressed()
                    } catch (e: Exception) {
                        // Fragment might be detached
                    }
                }
                return@observe
            }

            // Store the total for order items fallback
            orderTotal = order.totalAmount ?: 0.0
            isOrderCancelled = order.status == "CANCELLED"

            // Display order details
            orderIdText.text = "Order #${order.id}"
            orderStatusText.text = order.status
            
            // Format date
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                val date = inputFormat.parse(order.createdAt)
                orderDateText.text = date?.let { dateFormatter.format(it) } ?: order.createdAt
            } catch (e: Exception) {
                orderDateText.text = order.createdAt
            }

            orderTotalText.text = currencyFormatter.format(order.totalAmount ?: 0.0)

            // Set status badge background
            val statusBackground = when (order.status) {
                "PENDING" -> R.drawable.bg_status_pending
                "CONFIRMED" -> R.drawable.bg_status_confirmed
                "COMPLETED" -> R.drawable.bg_status_delivered
                "DELIVERED" -> R.drawable.bg_status_delivered
                "CANCELLED" -> R.drawable.bg_status_cancelled
                else -> R.drawable.bg_status_pending
            }
            orderStatusText.setBackgroundResource(statusBackground)

            // Payment method
            paymentCard.visibility = View.VISIBLE
            val paymentDisplay = when (order.paymentMethod) {
                "cash" -> "💵 Cash on Delivery"
                "card" -> "💳 Card Payment (Stripe)"
                else -> order.paymentMethod ?: "Unknown"
            }
            paymentMethodText.text = paymentDisplay

            // Payment Intent ID for card payments
            if (!order.paymentIntentId.isNullOrEmpty()) {
                paymentIdLayout.visibility = View.VISIBLE
                paymentIdText.text = order.paymentIntentId.take(20) + "..."
            } else {
                paymentIdLayout.visibility = View.GONE
            }

            // Delivery Information
            deliveryCard.visibility = View.VISIBLE

            // Delivery Address
            if (!order.deliveryAddress.isNullOrEmpty()) {
                addressLayout.visibility = View.VISIBLE
                deliveryAddressText.text = order.deliveryAddress
            } else {
                addressLayout.visibility = View.GONE
            }

            // Delivery location coordinates
            if (order.deliveryLatitude != null && order.deliveryLongitude != null) {
                deliveryLocationText.text = "📍 ${String.format(Locale.US, "%.6f", order.deliveryLatitude)}, ${String.format(Locale.US, "%.6f", order.deliveryLongitude)}"
            } else {
                deliveryLocationText.text = "📍 Not specified"
            }

            // Contact Phone
            if (!order.contactPhone.isNullOrEmpty()) {
                phoneLayout.visibility = View.VISIBLE
                contactPhoneText.text = "📞 ${order.contactPhone}"
            } else {
                phoneLayout.visibility = View.GONE
            }

            // Delivery Notes
            if (!order.deliveryNotes.isNullOrEmpty()) {
                notesLayout.visibility = View.VISIBLE
                deliveryNotesText.text = "📝 ${order.deliveryNotes}"
            } else {
                notesLayout.visibility = View.GONE
            }

            // Cancel button - only show for pending orders
            if (order.status == "PENDING") {
                cancelButton.visibility = View.VISIBLE
                cancelButton.setOnClickListener {
                    cancelButton.isEnabled = false
                    cancelButton.text = "Cancelling..."
                    orderViewModel.cancelOrder(order.id)
                }
            } else {
                cancelButton.visibility = View.GONE
            }

            // Also update order items if we have the total but no items yet
            // This handles the case where order loads before items observer fires
            orderViewModel.getOrderItems(orderId).value?.let { items ->
                if (items.isEmpty() && orderTotal > 0) {
                    adapter.submitList(listOf(
                        OrderItemDisplay("Order Total", 1, orderTotal)
                    ))
                }
            }
        }

        // Observe order items
        orderViewModel.getOrderItems(orderId).observe(viewLifecycleOwner) { orderItems ->
            if (orderItems.isNotEmpty()) {
                // Convert order items to display items
                val displayItems = orderItems.map { item ->
                    OrderItemDisplay(
                        productName = item.productName ?: "Product #${item.productId}",
                        quantity = item.quantity ?: 1,
                        price = (item.priceAtPurchase ?: 0.0) * (item.quantity ?: 1)
                    )
                }
                adapter.submitList(displayItems)
            } else if (orderTotal > 0) {
                // If no order items found but we have a total, show the total as a single item
                adapter.submitList(listOf(
                    OrderItemDisplay("Order Total", 1, orderTotal)
                ))
            } else {
                // Truly no items and no total
                adapter.submitList(listOf(
                    OrderItemDisplay("No items available", 0, 0.0)
                ))
            }
        }

        // Observe cancel status
        orderViewModel.cancelOrderStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(context, "Order cancelled successfully", Toast.LENGTH_SHORT).show()
                orderViewModel.refreshOrders()
                try {
                    if (isAdded && activity != null) {
                        requireActivity().onBackPressed()
                    }
                } catch (e: Exception) {
                    // Fragment might be detached, ignore
                }
            }
            result.onFailure {
                if (view != null) {
                    cancelButton.isEnabled = true
                    cancelButton.text = "Cancel Order"
                }
                Toast.makeText(context, "Failed to cancel: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
