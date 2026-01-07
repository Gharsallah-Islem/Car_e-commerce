package com.example.carpartsecom.ui.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.carpartsecom.MainActivity
import com.example.carpartsecom.R
import com.example.carpartsecom.ui.viewmodel.CartViewModel
import com.example.carpartsecom.ui.viewmodel.ProductViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.util.*

class ProductDetailFragment : Fragment() {
    private lateinit var productViewModel: ProductViewModel
    private lateinit var cartViewModel: CartViewModel
    private var productId: Long = 0
    private var currentQuantity = 1
    private var maxStock = 0
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        productId = arguments?.getLong("productId") ?: 0
        
        val factory = (requireActivity() as MainActivity).viewModelFactory
        productViewModel = ViewModelProvider(requireActivity(), factory)[ProductViewModel::class.java]
        cartViewModel = ViewModelProvider(requireActivity(), factory)[CartViewModel::class.java]
        
        // Setup toolbar
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        
        val productImage = view.findViewById<ImageView>(R.id.productImage)
        val productName = view.findViewById<TextView>(R.id.productName)
        val productCategory = view.findViewById<TextView>(R.id.productCategory)
        val productPrice = view.findViewById<TextView>(R.id.productPrice)
        val productRating = view.findViewById<TextView>(R.id.productRating)
        val productDescription = view.findViewById<TextView>(R.id.productDescription)
        val productStock = view.findViewById<TextView>(R.id.productStock)
        val stockIndicator = view.findViewById<View>(R.id.stockIndicator)
        val quantityText = view.findViewById<TextView>(R.id.quantityText)
        val decreaseButton = view.findViewById<MaterialButton>(R.id.decreaseButton)
        val increaseButton = view.findViewById<MaterialButton>(R.id.increaseButton)
        val addToCartButton = view.findViewById<MaterialButton>(R.id.addToCartButton)
        
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
        
        // Setup quantity controls
        decreaseButton.setOnClickListener {
            if (currentQuantity > 1) {
                currentQuantity--
                quantityText.text = currentQuantity.toString()
            }
        }
        
        increaseButton.setOnClickListener {
            if (currentQuantity < maxStock) {
                currentQuantity++
                quantityText.text = currentQuantity.toString()
            } else {
                Snackbar.make(view, "Maximum stock reached", Snackbar.LENGTH_SHORT).show()
            }
        }
        
        // Observe product details
        productViewModel.getProductById(productId).observe(viewLifecycleOwner) { product ->
            product?.let {
                maxStock = it.stockQuantity
                
                productName.text = it.name
                productCategory.text = it.category
                productPrice.text = currencyFormatter.format(it.price)
                productRating.text = String.format("%.1f", it.rating)
                productDescription.text = it.description
                
                if (it.stockQuantity > 0) {
                    productStock.text = "${it.stockQuantity} units available"
                    productStock.setTextColor(requireContext().getColor(R.color.success))
                    stockIndicator.backgroundTintList = ColorStateList.valueOf(requireContext().getColor(R.color.success))
                    addToCartButton.isEnabled = true
                } else {
                    productStock.text = "Out of stock"
                    productStock.setTextColor(requireContext().getColor(R.color.error))
                    stockIndicator.backgroundTintList = ColorStateList.valueOf(requireContext().getColor(R.color.error))
                    addToCartButton.isEnabled = false
                    addToCartButton.text = "Out of Stock"
                }
                
                Glide.with(this)
                    .load(it.imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(productImage)
            }
        }
        
        addToCartButton.setOnClickListener {
            addToCartButton.isEnabled = false
            val originalText = addToCartButton.text
            addToCartButton.text = "Adding..."
            cartViewModel.addToCart(productId, currentQuantity)
            
            // Re-enable after a delay to prevent double clicks
            addToCartButton.postDelayed({
                addToCartButton.isEnabled = maxStock > 0
                addToCartButton.text = originalText
            }, 1500)
        }
        
        // Observe add to cart status
        cartViewModel.addToCartStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess { _ ->
                Snackbar.make(view, "Added $currentQuantity item(s) to cart!", Snackbar.LENGTH_LONG)
                    .setAction("VIEW CART") {
                        requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
                            .selectedItemId = R.id.nav_cart
                    }
                    .show()
                
                // Reset quantity after successful add
                currentQuantity = 1
                quantityText.text = "1"
            }
            result.onFailure { error ->
                Snackbar.make(view, "Error: ${error.message}", Snackbar.LENGTH_SHORT).show()
                addToCartButton.isEnabled = maxStock > 0
                addToCartButton.text = "Add to Cart"
            }
        }
    }
}
