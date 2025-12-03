package com.integmobile.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.integmobile.data.api.RetrofitClient
import com.integmobile.data.db.AppDatabase
import com.integmobile.data.db.entity.OrderItem
import com.integmobile.data.repository.CartRepository
import com.integmobile.databinding.FragmentCartBinding
import com.integmobile.ui.cart.adapter.CartItemAdapter
import com.integmobile.ui.cart.viewmodel.CartViewModel
import com.integmobile.ui.cart.viewmodel.CartViewModelFactory
import com.integmobile.utils.formatPrice
import com.integmobile.utils.hide
import com.integmobile.utils.show
import com.integmobile.utils.showToast

/**
 * Shopping cart fragment with cart items and summary
 */
class CartFragment : Fragment() {
    
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: CartViewModel
    private lateinit var cartAdapter: CartItemAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModel()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        
        // Refresh cart summary
        viewModel.refreshCartSummary()
    }
    
    private fun setupViewModel() {
        val database = AppDatabase.getInstance(requireContext())
        val repository = CartRepository(
            RetrofitClient.cartService,
            database.cartItemDao()
        )
        val factory = CartViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[CartViewModel::class.java]
    }
    
    private fun setupRecyclerView() {
        cartAdapter = CartItemAdapter(
            onQuantityChanged = { cartItemId, quantity ->
                viewModel.updateQuantity(cartItemId, quantity)
            },
            onRemoveClick = { cartItemId ->
                viewModel.removeFromCart(cartItemId)
            }
        )
        
        binding.rvCartItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }
    
    private fun setupObservers() {
        // Cart items
        viewModel.cartItems.observe(viewLifecycleOwner) { items ->
            if (items.isEmpty()) {
                binding.layoutEmpty.show()
                binding.layoutCart.hide()
            } else {
                binding.layoutEmpty.hide()
                binding.layoutCart.show()
                cartAdapter.submitList(items)
            }
        }
        
        // Cart summary
        viewModel.cartSummary.observe(viewLifecycleOwner) { summary ->
            binding.tvSubtotal.text = summary.subtotal.formatPrice()
            binding.tvTax.text = summary.tax.formatPrice()
            binding.tvShipping.text = summary.shipping.formatPrice()
            binding.tvTotal.text = summary.total.formatPrice()
        }
        
        // Messages
        viewModel.message.observe(viewLifecycleOwner) { message ->
            showToast(message)
            viewModel.refreshCartSummary()
        }
    }
    
    private fun setupClickListeners() {
        binding.btnContinueShopping.setOnClickListener {
            // Navigate to products
            findNavController().navigate(com.integmobile.R.id.productsFragment)
        }
        
        binding.btnCheckout.setOnClickListener {
            // Navigate to checkout with cart items
            val cartItems = viewModel.cartItems.value ?: emptyList()
            if (cartItems.isEmpty()) {
                showToast("Cart is empty")
                return@setOnClickListener
            }
            
            // Convert cart items to order items
            val orderItems = cartItems.map { cartItem ->
                OrderItem(
                    productId = cartItem.productId,
                    productName = cartItem.productName,
                    price = cartItem.price,
                    quantity = cartItem.quantity,
                    imageUrl = cartItem.imageUrl
                )
            }
            
            // Navigate to checkout (you'll need to pass order items)
            findNavController().navigate(com.integmobile.R.id.checkoutFragment)
        }
        
        binding.btnClearCart.setOnClickListener {
            viewModel.clearCart()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
