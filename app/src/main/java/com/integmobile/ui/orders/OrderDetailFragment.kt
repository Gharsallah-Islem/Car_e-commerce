package com.integmobile.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.integmobile.R
import com.integmobile.data.api.RetrofitClient
import com.integmobile.data.db.AppDatabase
import com.integmobile.data.repository.OrderRepository
import com.integmobile.databinding.FragmentOrderDetailBinding
import com.integmobile.ui.orders.viewmodel.OrderViewModel
import com.integmobile.ui.orders.viewmodel.OrderViewModelFactory
import com.integmobile.utils.Constants
import com.integmobile.utils.Result
import com.integmobile.utils.formatPrice
import com.integmobile.utils.formatTimestamp
import com.integmobile.utils.hide
import com.integmobile.utils.show
import com.integmobile.utils.showToast

/**
 * Order detail fragment with cancel and claim options
 */
class OrderDetailFragment : Fragment() {
    
    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!
    
    private val args: OrderDetailFragmentArgs by navArgs()
    private lateinit var viewModel: OrderViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModel()
        setupObservers()
        setupClickListeners()
        
        // Load order details
        viewModel.loadOrderById(args.orderId)
    }
    
    private fun setupViewModel() {
        val database = AppDatabase.getInstance(requireContext())
        val repository = OrderRepository(
            RetrofitClient.orderService,
            database.orderDao()
        )
        val factory = OrderViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[OrderViewModel::class.java]
    }
    
    private fun setupObservers() {
        viewModel.orderDetail.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.show()
                }
                is Result.Success -> {
                    binding.progressBar.hide()
                    displayOrderDetails(result.data)
                }
                is Result.Error -> {
                    binding.progressBar.hide()
                    showToast(result.exception.message ?: "Failed to load order")
                }
            }
        }
        
        viewModel.message.observe(viewLifecycleOwner) { message ->
            showToast(message)
        }
    }
    
    private fun displayOrderDetails(order: com.integmobile.data.db.entity.Order) {
        binding.apply {
            tvOrderId.text = "Order #${order.id.take(8)}"
            tvOrderDate.text = order.createdAt.formatTimestamp()
            tvOrderStatus.text = order.status
            tvDeliveryAddress.text = order.deliveryAddress
            tvPhoneNumber.text = order.phoneNumber
            tvPaymentMethod.text = order.paymentMethod
            
            // Status color
            val statusColor = when (order.status) {
                Constants.OrderStatus.PENDING -> R.color.warning
                Constants.OrderStatus.CONFIRMED -> R.color.info
                Constants.OrderStatus.SHIPPED -> R.color.info
                Constants.OrderStatus.DELIVERED -> R.color.success
                Constants.OrderStatus.CANCELLED -> R.color.error
                else -> R.color.gray
            }
            tvOrderStatus.setTextColor(requireContext().getColor(statusColor))
            
            // Order items
            val itemsText = order.items.joinToString("\n\n") { item ->
                "${item.productName}\nQty: ${item.quantity} × ${item.price.formatPrice()} = ${(item.quantity * item.price).formatPrice()}"
            }
            tvOrderItems.text = itemsText
            
            // Totals
            tvSubtotal.text = order.subtotal.formatPrice()
            tvTax.text = order.tax.formatPrice()
            tvShipping.text = order.shipping.formatPrice()
            tvTotal.text = order.totalAmount.formatPrice()
            
            // Show/hide cancel button
            if (order.status == Constants.OrderStatus.PENDING || 
                order.status == Constants.OrderStatus.CONFIRMED) {
                btnCancelOrder.show()
            } else {
                btnCancelOrder.hide()
            }
            
            // Show claim button for delivered orders
            if (order.status == Constants.OrderStatus.DELIVERED) {
                btnSubmitClaim.show()
            } else {
                btnSubmitClaim.hide()
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnCancelOrder.setOnClickListener {
            viewModel.cancelOrder(args.orderId, "Customer requested cancellation")
        }
        
        binding.btnSubmitClaim.setOnClickListener {
            // Navigate to claim fragment (simplified - just submit a basic claim)
            viewModel.submitClaim(
                orderId = args.orderId,
                reason = "Product issue",
                description = "Issue with delivered product",
                photos = emptyList()
            )
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
