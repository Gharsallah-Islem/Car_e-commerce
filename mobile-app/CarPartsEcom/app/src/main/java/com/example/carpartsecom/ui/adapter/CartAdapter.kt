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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.carpartsecom.R
import com.example.carpartsecom.data.local.entities.CartItemWithProduct
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val onIncreaseClick: (CartItemWithProduct) -> Unit,
    private val onDecreaseClick: (CartItemWithProduct) -> Unit,
    private val onRemoveClick: (CartItemWithProduct) -> Unit
) : ListAdapter<CartItemWithProduct, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        private val productCategory: TextView = itemView.findViewById(R.id.productCategory)
        private val quantityText: TextView = itemView.findViewById(R.id.quantityText)
        private val totalPrice: TextView = itemView.findViewById(R.id.totalPrice)
        private val increaseButton: MaterialButton = itemView.findViewById(R.id.increaseButton)
        private val decreaseButton: MaterialButton = itemView.findViewById(R.id.decreaseButton)
        private val removeButton: MaterialButton = itemView.findViewById(R.id.removeButton)
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)

        fun bind(item: CartItemWithProduct) {
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
            
            productName.text = item.productName
            productCategory.text = item.productCategory
            productPrice.text = currencyFormat.format(item.productPrice)
            quantityText.text = item.quantity.toString()
            totalPrice.text = currencyFormat.format(item.totalPrice)
            
            // Load product image with Glide
            Glide.with(itemView.context)
                .load(item.productImageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
                .into(productImage)
            
            increaseButton.setOnClickListener { onIncreaseClick(item) }
            decreaseButton.setOnClickListener { onDecreaseClick(item) }
            removeButton.setOnClickListener { onRemoveClick(item) }
            
            // Disable increase if at stock limit
            increaseButton.isEnabled = item.quantity < item.productStockQuantity
        }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CartItemWithProduct>() {
        override fun areItemsTheSame(oldItem: CartItemWithProduct, newItem: CartItemWithProduct): Boolean {
            return oldItem.cartItemId == newItem.cartItemId
        }

        override fun areContentsTheSame(oldItem: CartItemWithProduct, newItem: CartItemWithProduct): Boolean {
            return oldItem == newItem
        }
    }
}
