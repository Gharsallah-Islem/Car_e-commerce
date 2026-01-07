package com.example.carpartsecom.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carpartsecom.MainActivity
import com.example.carpartsecom.R
import com.example.carpartsecom.data.local.AppDatabase
import com.example.carpartsecom.ui.adapter.ChatAdapter
import com.example.carpartsecom.ui.adapter.ProductAdapter
import com.example.carpartsecom.ui.viewmodel.CartViewModel
import com.example.carpartsecom.ui.viewmodel.ChatViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class AssistantFragment : Fragment() {

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var cartViewModel: CartViewModel
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var productAdapter: ProductAdapter

    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: TextInputEditText
    private lateinit var sendButton: MaterialButton
    private lateinit var typingIndicator: TextView
    private lateinit var recommendationsCard: MaterialCardView
    private lateinit var recommendationsRecyclerView: RecyclerView
    private lateinit var clearChatButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_assistant, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView)
        messageInput = view.findViewById(R.id.messageInput)
        sendButton = view.findViewById(R.id.sendButton)
        typingIndicator = view.findViewById(R.id.typingIndicator)
        recommendationsCard = view.findViewById(R.id.recommendationsCard)
        recommendationsRecyclerView = view.findViewById(R.id.recommendationsRecyclerView)
        clearChatButton = view.findViewById(R.id.clearChatButton)

        // Initialize ViewModels
        val database = AppDatabase.getDatabase(requireContext())
        chatViewModel = ViewModelProvider(
            this,
            ChatViewModelFactory(database.chatDao(), database.productDao())
        )[ChatViewModel::class.java]

        val factory = (requireActivity() as MainActivity).viewModelFactory
        cartViewModel = ViewModelProvider(requireActivity(), factory)[CartViewModel::class.java]

        setupMessagesRecyclerView()
        setupRecommendationsRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupMessagesRecyclerView() {
        chatAdapter = ChatAdapter()
        messagesRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
        }
    }

    private fun setupRecommendationsRecyclerView() {
        productAdapter = ProductAdapter { product ->
            // Navigate to product details
            val fragment = ProductDetailFragment().apply {
                arguments = Bundle().apply {
                    putLong("productId", product.id)
                }
            }
            (requireActivity() as MainActivity).navigateToFragment(fragment)
        }

        recommendationsRecyclerView.apply {
            adapter = productAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                chatViewModel.sendMessage(message)
                messageInput.text?.clear()
            }
        }

        clearChatButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear Chat")
                .setMessage("Are you sure you want to clear the conversation?")
                .setPositiveButton("Clear") { _, _ ->
                    chatViewModel.clearChat()
                    showSnackbar("Chat cleared")
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun observeViewModel() {
        chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
            chatAdapter.submitList(messages) {
                // Scroll to bottom when new messages arrive
                if (messages.isNotEmpty()) {
                    messagesRecyclerView.smoothScrollToPosition(messages.size - 1)
                }
            }
        }

        chatViewModel.isTyping.observe(viewLifecycleOwner) { isTyping ->
            typingIndicator.visibility = if (isTyping) View.VISIBLE else View.GONE
        }

        chatViewModel.recommendedProducts.observe(viewLifecycleOwner) { products ->
            if (products.isNotEmpty()) {
                recommendationsCard.visibility = View.VISIBLE
                productAdapter.submitList(products)
            } else {
                recommendationsCard.visibility = View.GONE
            }
        }

        cartViewModel.addToCartStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                // Item added successfully
            }
            result.onFailure { error ->
                showSnackbar("Failed to add to cart: ${error.message}")
            }
        }
    }

    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    // Custom ViewModelFactory for ChatViewModel
    class ChatViewModelFactory(
        private val chatDao: com.example.carpartsecom.data.local.dao.ChatDao,
        private val productDao: com.example.carpartsecom.data.local.dao.ProductDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                return ChatViewModel(chatDao, productDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

