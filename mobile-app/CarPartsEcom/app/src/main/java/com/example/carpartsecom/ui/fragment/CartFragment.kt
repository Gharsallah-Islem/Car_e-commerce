package com.example.carpartsecom.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carpartsecom.MainActivity
import com.example.carpartsecom.R
import com.example.carpartsecom.ui.adapter.CartAdapter
import com.example.carpartsecom.ui.viewmodel.CartViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

class CartFragment : Fragment() {
    private lateinit var cartViewModel: CartViewModel
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = (requireActivity() as MainActivity).viewModelFactory
        cartViewModel = ViewModelProvider(requireActivity(), factory)[CartViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.cartRecyclerView)
        val checkoutButton = view.findViewById<MaterialButton>(R.id.checkoutButton)
        val totalPriceText = view.findViewById<TextView>(R.id.totalPriceText)
        val emptyLayout = view.findViewById<View>(R.id.emptyCartLayout)
        val checkoutCard = view.findViewById<View>(R.id.checkoutCard)

        adapter = CartAdapter(
            onIncreaseClick = { item ->
                cartViewModel.updateCart(item.productId, item.quantity + 1)
            },
            onDecreaseClick = { item ->
                if (item.quantity > 1) {
                    cartViewModel.updateCart(item.productId, item.quantity - 1)
                } else {
                    cartViewModel.removeFromCart(item.productId)
                }
            },
            onRemoveClick = { item ->
                cartViewModel.removeFromCart(item.productId)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        checkoutButton.setOnClickListener {
            (requireActivity() as MainActivity).navigateToFragment(CheckoutFragment())
        }

        // Observe Cart Items with Product Details
        cartViewModel.cartItemsWithProducts.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            if (items.isEmpty()) {
                emptyLayout?.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                checkoutCard?.visibility = View.GONE
            } else {
                emptyLayout?.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                checkoutCard?.visibility = View.VISIBLE
                
                // Calculate total
                val total = items.sumOf { it.totalPrice }
                totalPriceText?.text = String.format("$%.2f", total)
            }
        }

        // Observe update status
        cartViewModel.updateCartStatus.observe(viewLifecycleOwner) { result ->
            result.onFailure {
                showSnackbar("Update failed: ${it.message}")
            }
        }

        // Observe remove status
        cartViewModel.removeFromCartStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                showSnackbar("Item removed from cart")
            }
            result.onFailure {
                showSnackbar("Remove failed: ${it.message}")
            }
        }
    }
    
    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }
}
