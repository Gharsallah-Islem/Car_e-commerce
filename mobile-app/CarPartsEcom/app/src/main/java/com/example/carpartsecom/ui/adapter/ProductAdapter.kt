package com.example.carpartsecom.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carpartsecom.R
import com.example.carpartsecom.data.local.entities.ProductEntity

class ProductAdapter(
    private val onProductClick: (ProductEntity) -> Unit
) : ListAdapter<ProductEntity, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        private val productRating: TextView = itemView.findViewById(R.id.productRating)
        private val productCategory: TextView = itemView.findViewById(R.id.productCategory)
        private val stockText: TextView = itemView.findViewById(R.id.stockText)
        private val stockIndicator: View = itemView.findViewById(R.id.stockIndicator)
        
        fun bind(product: ProductEntity) {
            productName.text = product.name
            productPrice.text = String.format("$%.2f", product.price)
            productRating.text = String.format("%.1f", product.rating)
            productCategory.text = product.category
            
            // Stock status
            if (product.stockQuantity > 0) {
                stockText.text = "${product.stockQuantity} in stock"
                stockText.setTextColor(itemView.context.getColor(R.color.success))
                stockIndicator.backgroundTintList = 
                    android.content.res.ColorStateList.valueOf(itemView.context.getColor(R.color.success))
            } else {
                stockText.text = "Out of stock"
                stockText.setTextColor(itemView.context.getColor(R.color.error))
                stockIndicator.backgroundTintList = 
                    android.content.res.ColorStateList.valueOf(itemView.context.getColor(R.color.error))
            }
            
            Glide.with(itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
                .into(productImage)
            
            itemView.setOnClickListener {
                onProductClick(product)
            }
        }
    }
    
    class ProductDiffCallback : DiffUtil.ItemCallback<ProductEntity>() {
        override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return oldItem == newItem
        }
    }
}
