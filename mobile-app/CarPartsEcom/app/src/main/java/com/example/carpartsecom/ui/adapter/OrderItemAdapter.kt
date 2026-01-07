package com.example.carpartsecom.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.carpartsecom.R
import java.text.NumberFormat
import java.util.*

data class OrderItemDisplay(
    val productName: String,
    val quantity: Int,
    val price: Double
)

class OrderItemAdapter : ListAdapter<OrderItemDisplay, OrderItemAdapter.OrderItemViewHolder>(OrderItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_detail_product, parent, false)
        return OrderItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OrderItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productQuantity: TextView = itemView.findViewById(R.id.productQuantity)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)

        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

        fun bind(item: OrderItemDisplay) {
            productName.text = item.productName
            productQuantity.text = "Qty: ${item.quantity}"
            productPrice.text = currencyFormatter.format(item.price)
        }
    }

    class OrderItemDiffCallback : DiffUtil.ItemCallback<OrderItemDisplay>() {
        override fun areItemsTheSame(oldItem: OrderItemDisplay, newItem: OrderItemDisplay): Boolean {
            return oldItem.productName == newItem.productName
        }

        override fun areContentsTheSame(oldItem: OrderItemDisplay, newItem: OrderItemDisplay): Boolean {
            return oldItem == newItem
        }
    }
}
