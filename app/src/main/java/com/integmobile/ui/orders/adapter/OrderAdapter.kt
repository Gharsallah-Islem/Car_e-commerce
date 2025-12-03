package com.integmobile.ui.orders.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.integmobile.R
import com.integmobile.data.db.entity.Order
import com.integmobile.databinding.ItemOrderBinding
import com.integmobile.utils.Constants
import com.integmobile.utils.formatPrice
import com.integmobile.utils.formatTimestamp

/**
 * RecyclerView adapter for orders list
 */
class OrderAdapter(
    private val onOrderClick: (Order) -> Unit
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding, onOrderClick)
    }
    
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class OrderViewHolder(
        private val binding: ItemOrderBinding,
        private val onOrderClick: (Order) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(order: Order) {
            binding.apply {
                tvOrderId.text = "Order #${order.id.take(8)}"
                tvOrderDate.text = order.createdAt.formatTimestamp()
                tvOrderTotal.text = order.totalAmount.formatPrice()
                tvItemCount.text = "${order.items.size} items"
                
                // Status badge
                tvOrderStatus.text = order.status
                val statusColor = when (order.status) {
                    Constants.OrderStatus.PENDING -> R.color.warning
                    Constants.OrderStatus.CONFIRMED -> R.color.info
                    Constants.OrderStatus.SHIPPED -> R.color.info
                    Constants.OrderStatus.DELIVERED -> R.color.success
                    Constants.OrderStatus.CANCELLED -> R.color.error
                    else -> R.color.gray
                }
                tvOrderStatus.setBackgroundColor(root.context.getColor(statusColor))
                
                // Click listener
                root.setOnClickListener {
                    onOrderClick(order)
                }
            }
        }
    }
    
    private class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}
