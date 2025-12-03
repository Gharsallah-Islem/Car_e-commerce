package com.integmobile.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.integmobile.CarPartsApplication
import com.integmobile.R
import com.integmobile.data.api.RetrofitClient
import com.integmobile.data.db.AppDatabase
import com.integmobile.data.repository.CartRepository
import com.integmobile.data.repository.ProductRepository
import com.integmobile.databinding.FragmentProductDetailBinding
import com.integmobile.ui.cart.viewmodel.CartViewModel
import com.integmobile.ui.cart.viewmodel.CartViewModelFactory
import com.integmobile.ui.products.viewmodel.ProductViewModel
import com.integmobile.ui.products.viewmodel.ProductViewModelFactory
import com.integmobile.utils.Result
import com.integmobile.utils.formatPrice
import com.integmobile.utils.hide
import com.integmobile.utils.show
import com.integmobile.utils.showToast

/**
 * Product detail fragment with add to cart functionality
 */
class ProductDetailFragment : Fragment() {
    
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    
    private val args: ProductDetailFragmentArgs by navArgs()
    private lateinit var productViewModel: ProductViewModel
    private lateinit var cartViewModel: CartViewModel
    
    private var currentQuantity = 1
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModels()
        setupObservers()
        setupClickListeners()
        
        // Load product details
        productViewModel.loadProductById(args.productId)
    }
    
    private fun setupViewModels() {
        val database = AppDatabase.getInstance(requireContext())
        
        // Product ViewModel
        val productRepository = ProductRepository(
            RetrofitClient.productService,
            database.productDao()
        )
        val productFactory = ProductViewModelFactory(productRepository)
        productViewModel = ViewModelProvider(this, productFactory)[ProductViewModel::class.java]
        
        // Cart ViewModel
        val cartRepository = CartRepository(
            RetrofitClient.cartService,
            database.cartItemDao()
        )
        val cartFactory = CartViewModelFactory(cartRepository)
        cartViewModel = ViewModelProvider(this, cartFactory)[CartViewModel::class.java]
    }
    
    private fun setupObservers() {
        productViewModel.productDetail.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.show()
                }
                is Result.Success -> {
                    binding.progressBar.hide()
                    displayProductDetails(result.data)
                }
                is Result.Error -> {
                    binding.progressBar.hide()
                    showToast(result.exception.message ?: "Failed to load product")
                }
            }
        }
        
        cartViewModel.message.observe(viewLifecycleOwner) { message ->
            showToast(message)
        }
    }
    
    private fun displayProductDetails(product: com.integmobile.data.db.entity.Product) {
        binding.apply {
            tvProductName.text = product.name
            tvProductBrand.text = product.brand
            tvProductPrice.text = product.price.formatPrice()
            tvProductDescription.text = product.description
            tvQuantityAvailable.text = "Available: ${product.quantityAvailable}"
            
            // Load first image
            Glide.with(requireContext())
                .load(product.imageUrl.firstOrNull())
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(ivProductImage)
            
            // Stock status
            if (product.inStock) {
                tvStockStatus.text = "In Stock"
                tvStockStatus.setTextColor(requireContext().getColor(R.color.success))
                btnAddToCart.isEnabled = true
            } else {
                tvStockStatus.text = "Out of Stock"
                tvStockStatus.setTextColor(requireContext().getColor(R.color.error))
                btnAddToCart.isEnabled = false
            }
            
            // Specifications
            val specsText = product.specifications.entries.joinToString("\n") { 
                "${it.key}: ${it.value}" 
            }
            tvSpecifications.text = specsText
            
            // Compatibility
            val compatibilityText = product.compatibility.joinToString(", ")
            tvCompatibility.text = compatibilityText
        }
    }
    
    private fun setupClickListeners() {
        binding.btnMinus.setOnClickListener {
            if (currentQuantity > 1) {
                currentQuantity--
                binding.tvQuantity.text = currentQuantity.toString()
            }
        }
        
        binding.btnPlus.setOnClickListener {
            currentQuantity++
            binding.tvQuantity.text = currentQuantity.toString()
        }
        
        binding.btnAddToCart.setOnClickListener {
            val product = (productViewModel.productDetail.value as? Result.Success)?.data
            product?.let {
                cartViewModel.addToCart(
                    productId = it.id,
                    productName = it.name,
                    price = it.price,
                    quantity = currentQuantity,
                    imageUrl = it.imageUrl.firstOrNull() ?: ""
                )
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
