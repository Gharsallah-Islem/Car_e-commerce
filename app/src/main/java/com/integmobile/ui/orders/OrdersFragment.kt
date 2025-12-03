package com.integmobile.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.integmobile.data.api.RetrofitClient
import com.integmobile.data.db.AppDatabase
import com.integmobile.data.repository.OrderRepository
import com.integmobile.databinding.FragmentOrdersBinding
import com.integmobile.ui.orders.adapter.OrderAdapter
import com.integmobile.ui.orders.viewmodel.OrderViewModel
import com.integmobile.ui.orders.viewmodel.OrderViewModelFactory
import com.integmobile.utils.Constants
import com.integmobile.utils.hide
import com.integmobile.utils.show
import com.integmobile.utils.showToast

/**
 * Orders fragment with tabs for filtering by status
 */
class OrdersFragment : Fragment() {
    
    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: OrderViewModel
    private lateinit var orderAdapter: OrderAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModel()
        setupRecyclerView()
        setupTabs()
        setupObservers()
        
        // Sync orders
        viewModel.syncOrders()
    }
    
    private fun setupViewModel() {
        val database = AppDatabase.getInstance(requireContext())
        val repository = OrderRepository(
            RetrofitClient.orderService,
            database.orderDao()
        )
        val factory = OrderViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[OrderViewModel::class.java]
    }
    
    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter { order ->
            // Navigate to order detail
            val action = OrdersFragmentDirections.actionOrdersToDetail(order.id)
            findNavController().navigate(action)
        }
        
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
    }
    
    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.filterOrdersByStatus(null) // All
                    1 -> viewModel.filterOrdersByStatus(Constants.OrderStatus.PENDING)
                    2 -> viewModel.filterOrdersByStatus(Constants.OrderStatus.CONFIRMED)
                    3 -> viewModel.filterOrdersByStatus(Constants.OrderStatus.DELIVERED)
                    4 -> viewModel.filterOrdersByStatus(Constants.OrderStatus.CANCELLED)
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun setupObservers() {
        // All orders
        viewModel.allOrders.observe(viewLifecycleOwner) { orders ->
            if (binding.tabLayout.selectedTabPosition == 0) {
                displayOrders(orders)
            }
        }
        
        // Filtered orders
        viewModel.filteredOrders.observe(viewLifecycleOwner) { orders ->
            if (binding.tabLayout.selectedTabPosition != 0) {
                displayOrders(orders)
            }
        }
        
        // Messages
        viewModel.message.observe(viewLifecycleOwner) { message ->
            showToast(message)
        }
    }
    
    private fun displayOrders(orders: List<com.integmobile.data.db.entity.Order>) {
        if (orders.isEmpty()) {
            binding.tvEmpty.show()
            binding.rvOrders.hide()
        } else {
            binding.tvEmpty.hide()
            binding.rvOrders.show()
            orderAdapter.submitList(orders)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
