package com.integmobile.ui.products.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.integmobile.R
import com.integmobile.data.db.entity.Product
import com.integmobile.databinding.ItemProductBinding
import com.integmobile.utils.formatPrice

/**
 * RecyclerView adapter for product list
 * Uses ListAdapter with DiffUtil for efficient updates
 */
class ProductAdapter(
    private val onProductClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding, onProductClick)
    }
    
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ProductViewHolder(
        private val binding: ItemProductBinding,
        private val onProductClick: (Product) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(product: Product) {
            binding.apply {
                tvProductName.text = product.name
                tvProductPrice.text = product.price.formatPrice()
                tvProductBrand.text = product.brand
                
                // Show discount if available
                if (product.discount != null && product.discount > 0) {
                    tvProductDiscount.text = "${product.discount}% OFF"
                    tvProductDiscount.visibility = android.view.View.VISIBLE
                    tvProductOriginalPrice.text = product.originalPrice?.formatPrice() ?: ""
                    tvProductOriginalPrice.visibility = android.view.View.VISIBLE
                } else {
                    tvProductDiscount.visibility = android.view.View.GONE
                    tvProductOriginalPrice.visibility = android.view.View.GONE
                }
                
                // Stock status
                if (product.inStock) {
                    tvStockStatus.text = "In Stock"
                    tvStockStatus.setTextColor(root.context.getColor(R.color.success))
                } else {
                    tvStockStatus.text = "Out of Stock"
                    tvStockStatus.setTextColor(root.context.getColor(R.color.error))
                }
                
                // Load image with Glide
                Glide.with(root.context)
                    .load(product.imageUrl.firstOrNull())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(ivProductImage)
                
                // Click listener
                root.setOnClickListener {
                    onProductClick(product)
                }
            }
        }
    }
    
    private class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
