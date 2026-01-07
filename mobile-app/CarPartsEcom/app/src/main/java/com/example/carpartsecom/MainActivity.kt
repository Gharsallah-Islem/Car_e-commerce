package com.example.carpartsecom

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.carpartsecom.data.local.AppDatabase
import com.example.carpartsecom.data.remote.RetrofitClient
import com.example.carpartsecom.data.remote.api.*
import com.example.carpartsecom.data.repository.*
import com.example.carpartsecom.ui.fragment.*
import com.example.carpartsecom.ui.viewmodel.AuthViewModel
import com.example.carpartsecom.ui.viewmodel.ViewModelFactory
import com.example.carpartsecom.util.StripePaymentHelper
import com.example.carpartsecom.util.TokenManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var authViewModel: AuthViewModel
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var tokenManager: TokenManager
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var database: AppDatabase
    
    // Stripe Payment
    var stripePaymentHelper: StripePaymentHelper? = null
        private set
    private var stripePaymentCallback: ((StripePaymentHelper.PaymentResult) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize Stripe SDK
        StripePaymentHelper.initialize(this)

        // Initialize Stripe Payment Helper - must be done in onCreate before fragments
        stripePaymentHelper = StripePaymentHelper(this) { result ->
            stripePaymentCallback?.invoke(result)
        }

        bottomNav = findViewById(R.id.bottomNavigation)
        
        // Initialize dependencies
        database = AppDatabase.getDatabase(this)
        tokenManager = TokenManager(this)
        val retrofit = RetrofitClient.getClient()
        
        // Create repositories
        val authRepository = AuthRepository(
            retrofit.create(AuthService::class.java),
            database.userDao(),
            tokenManager
        )
        val productRepository = ProductRepository(
            retrofit.create(ProductService::class.java),
            database.productDao()
        )
        val cartRepository = CartRepository(
            retrofit.create(CartService::class.java),
            database.cartDao(),
            tokenManager
        )
        val orderRepository = OrderRepository(
            retrofit.create(OrderService::class.java),
            database.orderDao(),
            database.orderItemDao(),
            database.cartDao(),
            tokenManager
        )
        val paymentRepository = PaymentRepository(
            retrofit.create(PaymentService::class.java),
            tokenManager
        )
        val claimRepository = ClaimRepository(
            retrofit.create(ClaimService::class.java),
            database.claimDao(),
            tokenManager
        )
        val userRepository = UserRepository(
            retrofit.create(UserService::class.java),
            tokenManager
        )
        
        // Create ViewModelFactory
        viewModelFactory = ViewModelFactory(
            authRepository,
            productRepository,
            cartRepository,
            orderRepository,
            paymentRepository,
            claimRepository,
            userRepository
        )
        
        // Get AuthViewModel
        authViewModel = ViewModelProvider(this, viewModelFactory)[AuthViewModel::class.java]
        
        // Setup bottom navigation with animations
        bottomNav.setOnItemSelectedListener { item ->
            // Don't handle navigation if bottom nav is hidden (during logout)
            if (bottomNav.visibility != View.VISIBLE) {
                return@setOnItemSelectedListener false
            }

            val fragment: Fragment = when (item.itemId) {
                R.id.nav_products -> ProductListFragment()
                R.id.nav_cart -> CartFragment()
                R.id.nav_orders -> OrderListFragment()
                R.id.nav_assistant -> AssistantFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> return@setOnItemSelectedListener false
            }
            replaceFragmentWithAnimation(fragment, false)
            true
        }

        // FORCE: Hide bottom nav and show login on start
        bottomNav.visibility = View.GONE

        // Check if user has valid token
        if (tokenManager.hasToken()) {
            showMainApp()
        } else {
            lifecycleScope.launch {
                database.userDao().clearUser()
            }
            showLogin()
        }
    }

    private fun showLogin() {
        bottomNav.visibility = View.GONE
        replaceFragmentWithAnimation(LoginFragment(), false)
    }

    fun showMainApp() {
        bottomNav.visibility = View.VISIBLE
        bottomNav.selectedItemId = R.id.nav_products
    }

    private fun replaceFragmentWithAnimation(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }

    fun navigateToFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        replaceFragmentWithAnimation(fragment, addToBackStack)
    }

    fun logout() {
        // First, immediately hide bottom nav
        bottomNav.visibility = View.GONE

        // Clear the back stack
        supportFragmentManager.popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)

        // Navigate to login immediately (use commitNow for synchronous execution)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .commitNow()

        // Clear data in background
        lifecycleScope.launch {
            tokenManager.clearToken()
            database.userDao().clearUser()
            database.cartDao().clearCart()
            database.orderDao().clearOrders()
            database.orderItemDao().clearOrderItems()
        }

        showSnackbar("Logged out successfully")
    }

    fun showSnackbar(message: String) {
        Snackbar.make(findViewById(R.id.fragment_container), message, Snackbar.LENGTH_SHORT).show()
    }

    /**
     * Set callback for Stripe payment result
     * Call this from fragment before initiating payment
     */
    fun setStripePaymentCallback(callback: ((StripePaymentHelper.PaymentResult) -> Unit)?) {
        stripePaymentCallback = callback
    }
}