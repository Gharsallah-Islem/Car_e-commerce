package com.integmobile.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.integmobile.CarPartsApplication
import com.integmobile.data.api.RetrofitClient
import com.integmobile.data.db.AppDatabase
import com.integmobile.data.repository.ProductRepository
import com.integmobile.databinding.FragmentProductsBinding
import com.integmobile.ui.products.adapter.ProductAdapter
import com.integmobile.ui.products.viewmodel.ProductViewModel
import com.integmobile.ui.products.viewmodel.ProductViewModelFactory
import com.integmobile.utils.Result
import com.integmobile.utils.hide
import com.integmobile.utils.show
import com.integmobile.utils.showToast

/**
 * Products listing fragment with search and filter
 */
class ProductsFragment : Fragment() {
    
    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ProductViewModel
    private lateinit var productAdapter: ProductAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModel()
        setupRecyclerView()
        setupSearchView()
        setupObservers()
        setupClickListeners()
        
        // Load products
        viewModel.loadProducts()
        viewModel.loadBrands()
        viewModel.loadCategories()
    }
    
    private fun setupViewModel() {
        val database = AppDatabase.getInstance(requireContext())
        val repository = ProductRepository(
            RetrofitClient.productService,
            database.productDao()
        )
        val factory = ProductViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProductViewModel::class.java]
    }
    
    private fun setupRecyclerView() {
        productAdapter = ProductAdapter { product ->
            // Navigate to product detail
            val action = ProductsFragmentDirections.actionProductsToDetail(product.id)
            findNavController().navigate(action)
        }
        
        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
        }
    }
    
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotEmpty()) {
                        viewModel.searchProducts(it)
                    } else {
                        viewModel.loadProducts()
                    }
                }
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                // Optional: implement real-time search
                return true
            }
        })
    }
    
    private fun setupObservers() {
        viewModel.products.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.show()
                    binding.tvEmpty.hide()
                }
                is Result.Success -> {
                    binding.progressBar.hide()
                    if (result.data.isEmpty()) {
                        binding.tvEmpty.show()
                        binding.rvProducts.hide()
                    } else {
                        binding.tvEmpty.hide()
                        binding.rvProducts.show()
                        productAdapter.submitList(result.data)
                    }
                }
                is Result.Error -> {
                    binding.progressBar.hide()
                    showToast(result.exception.message ?: "Failed to load products")
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnFilter.setOnClickListener {
            // Show filter dialog
            showToast("Filter feature - to be implemented")
        }
        
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadProducts()
            binding.swipeRefresh.isRefreshing = false
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
