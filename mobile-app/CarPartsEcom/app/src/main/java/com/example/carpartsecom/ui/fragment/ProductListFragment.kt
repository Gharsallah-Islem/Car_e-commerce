package com.example.carpartsecom.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carpartsecom.MainActivity
import com.example.carpartsecom.R
import com.example.carpartsecom.ui.adapter.ProductAdapter
import com.example.carpartsecom.ui.viewmodel.ProductViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class ProductListFragment : Fragment() {
    private lateinit var productViewModel: ProductViewModel
    private lateinit var adapter: ProductAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_list, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val factory = (requireActivity() as MainActivity).viewModelFactory
        productViewModel = ViewModelProvider(requireActivity(), factory)[ProductViewModel::class.java]
        
        // Setup toolbar with logout button
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    (requireActivity() as MainActivity).logout()
                    true
                }
                else -> false
            }
        }
        
        val recyclerView = view.findViewById<RecyclerView>(R.id.productsRecyclerView)
        val sortSpinner = view.findViewById<Spinner>(R.id.sortSpinner)
        val searchEditText = view.findViewById<TextInputEditText>(R.id.searchEditText)
        val clearSearchButton = view.findViewById<MaterialButton>(R.id.clearSearchButton)
        val categoryChipGroup = view.findViewById<ChipGroup>(R.id.categoryChipGroup)
        
        // Setup RecyclerView
        adapter = ProductAdapter { product ->
            val bundle = Bundle().apply {
                putLong("productId", product.id)
            }
            val detailFragment = ProductDetailFragment().apply {
                arguments = bundle
            }
            (requireActivity() as MainActivity).navigateToFragment(detailFragment)
        }
        
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = adapter
        
        // Setup search functionality
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    clearSearchButton.visibility = View.VISIBLE
                    productViewModel.searchProducts(query)
                    // Uncheck all category chips when searching
                    categoryChipGroup.clearCheck()
                } else {
                    clearSearchButton.visibility = View.GONE
                    productViewModel.clearFilters()
                }
            }
        })
        
        clearSearchButton.setOnClickListener {
            searchEditText.text?.clear()
            clearSearchButton.visibility = View.GONE
            productViewModel.clearFilters()
        }
        
        // Setup category chips
        categoryChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                productViewModel.clearFilters()
                return@setOnCheckedStateChangeListener
            }
            
            val checkedChip = view.findViewById<Chip>(checkedIds[0])
            val category = when (checkedChip.id) {
                R.id.chipAll -> {
                    productViewModel.clearFilters()
                    return@setOnCheckedStateChangeListener
                }
                R.id.chipBrakes -> "Brakes"
                R.id.chipEngine -> "Engine"
                R.id.chipIgnition -> "Ignition"
                R.id.chipElectrical -> "Electrical"
                else -> ""
            }
            
            if (category.isNotEmpty()) {
                // Clear search when filtering by category
                searchEditText.text?.clear()
                productViewModel.filterByCategory(category)
            }
        }
        
        // Setup sort spinner
        val sortOptions = arrayOf("All Products", "Price: Low → High", "Price: High → Low", "Best Rating", "Name A-Z")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = spinnerAdapter
        
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val sortType = when(position) {
                    0 -> "all"
                    1 -> "price_asc"
                    2 -> "price_desc"
                    3 -> "rating"
                    4 -> "name"
                    else -> "all"
                }
                productViewModel.setSortType(sortType)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Observe products
        productViewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
        }
    }
}
