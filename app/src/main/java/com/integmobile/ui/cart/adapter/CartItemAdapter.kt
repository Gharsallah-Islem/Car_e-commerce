package com.integmobile.ui.cart.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.integmobile.R
import com.integmobile.data.db.entity.CartItem
import com.integmobile.databinding.ItemCartBinding
import com.integmobile.utils.formatPrice

/**
 * RecyclerView adapter for cart items
 * Provides quantity controls and remove functionality
 */
class CartItemAdapter(
    private val onQuantityChanged: (String, Int) -> Unit,
    private val onRemoveClick: (String) -> Unit
) : ListAdapter<CartItem, CartItemAdapter.CartItemViewHolder>(CartItemDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartItemViewHolder(binding, onQuantityChanged, onRemoveClick)
    }
    
    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class CartItemViewHolder(
        private val binding: ItemCartBinding,
        private val onQuantityChanged: (String, Int) -> Unit,
        private val onRemoveClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(cartItem: CartItem) {
            binding.apply {
                tvProductName.text = cartItem.productName
                tvProductPrice.text = cartItem.price.formatPrice()
                tvQuantity.text = cartItem.quantity.toString()
                
                val subtotal = cartItem.price * cartItem.quantity
                tvSubtotal.text = subtotal.formatPrice()
                
                // Load image
                Glide.with(root.context)
                    .load(cartItem.imageUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(ivProductImage)
                
                // Quantity controls
                btnMinus.setOnClickListener {
                    val newQuantity = cartItem.quantity - 1
                    if (newQuantity > 0) {
                        onQuantityChanged(cartItem.id, newQuantity)
                    }
                }
                
                btnPlus.setOnClickListener {
                    val newQuantity = cartItem.quantity + 1
                    onQuantityChanged(cartItem.id, newQuantity)
                }
                
                // Remove button
                btnRemove.setOnClickListener {
                    onRemoveClick(cartItem.id)
                }
            }
        }
    }
    
    private class CartItemDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}
