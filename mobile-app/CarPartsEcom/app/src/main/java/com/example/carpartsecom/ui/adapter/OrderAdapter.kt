package com.example.carpartsecom.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.carpartsecom.R
import com.example.carpartsecom.data.local.entities.OrderEntity
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(
    private val onCancelClick: (OrderEntity) -> Unit,
    private val onViewDetailsClick: (OrderEntity) -> Unit
) : ListAdapter<OrderEntity, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderId: TextView = itemView.findViewById(R.id.orderId)
        private val orderDate: TextView = itemView.findViewById(R.id.orderDate)
        private val orderStatus: TextView = itemView.findViewById(R.id.orderStatus)
        private val orderTotal: TextView = itemView.findViewById(R.id.orderTotal)
        private val orderAddress: TextView = itemView.findViewById(R.id.orderAddress)
        private val cancelButton: MaterialButton = itemView.findViewById(R.id.cancelButton)
        private val viewDetailsButton: MaterialButton = itemView.findViewById(R.id.viewDetailsButton)

        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
        private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)

        fun bind(order: OrderEntity) {
            orderId.text = "Order #${order.id}"
            
            // Format date if possible
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                val date = inputFormat.parse(order.createdAt)
                orderDate.text = date?.let { dateFormatter.format(it) } ?: order.createdAt
            } catch (e: Exception) {
                orderDate.text = order.createdAt
            }
            
            orderStatus.text = order.status
            orderTotal.text = currencyFormatter.format(order.totalAmount)
            
            // Display delivery location if available
            if (order.deliveryLatitude != null && order.deliveryLongitude != null) {
                orderAddress.text = "📍 Location: ${order.deliveryLatitude}, ${order.deliveryLongitude}"
                orderAddress.visibility = View.VISIBLE
            } else {
                orderAddress.visibility = View.GONE
            }
            
            // Set status badge background based on status
            val statusBackground = when (order.status) {
                "PENDING" -> R.drawable.bg_status_pending
                "CONFIRMED" -> R.drawable.bg_status_confirmed
                "DELIVERED" -> R.drawable.bg_status_delivered
                "CANCELLED" -> R.drawable.bg_status_cancelled
                else -> R.drawable.bg_status_pending
            }
            orderStatus.setBackgroundResource(statusBackground)

            // Only allow cancelling pending orders
            if (order.status == "PENDING") {
                cancelButton.visibility = View.VISIBLE
                cancelButton.setOnClickListener { onCancelClick(order) }
            } else {
                cancelButton.visibility = View.GONE
            }
            
            viewDetailsButton.setOnClickListener {
                onViewDetailsClick(order)
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<OrderEntity>() {
        override fun areItemsTheSame(oldItem: OrderEntity, newItem: OrderEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: OrderEntity, newItem: OrderEntity): Boolean {
            return oldItem == newItem
        }
    }
}
