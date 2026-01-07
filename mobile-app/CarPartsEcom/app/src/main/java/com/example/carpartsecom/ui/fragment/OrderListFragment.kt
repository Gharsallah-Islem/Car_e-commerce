package com.example.carpartsecom.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carpartsecom.MainActivity
import com.example.carpartsecom.R
import com.example.carpartsecom.ui.adapter.OrderAdapter
import com.example.carpartsecom.ui.viewmodel.OrderViewModel

class OrderListFragment : Fragment() {
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var adapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = (requireActivity() as MainActivity).viewModelFactory
        orderViewModel = ViewModelProvider(requireActivity(), factory)[OrderViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.ordersRecyclerView)

        adapter = OrderAdapter(
            onCancelClick = { order ->
                orderViewModel.cancelOrder(order.id)
            },
            onViewDetailsClick = { order ->
                val orderDetailsFragment = OrderDetailsFragment.newInstance(order.id)
                (requireActivity() as MainActivity).navigateToFragment(orderDetailsFragment)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Observe Orders
        orderViewModel.orders.observe(viewLifecycleOwner) { orders ->
            adapter.submitList(orders)
        }

        // Observe Cancel Status
        orderViewModel.cancelOrderStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(context, "Order cancelled", Toast.LENGTH_SHORT).show()
                orderViewModel.refreshOrders() // Refresh list
            }
            result.onFailure {
                Toast.makeText(context, "Failed to cancel: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Initial refresh
        orderViewModel.refreshOrders()
    }
}
