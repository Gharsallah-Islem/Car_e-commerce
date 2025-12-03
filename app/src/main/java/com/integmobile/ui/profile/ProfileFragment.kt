package com.integmobile.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.integmobile.CarPartsApplication
import com.integmobile.data.api.RetrofitClient
import com.integmobile.data.db.AppDatabase
import com.integmobile.data.repository.AuthRepository
import com.integmobile.databinding.FragmentProfileBinding
import com.integmobile.ui.auth.LoginActivity
import kotlinx.coroutines.launch

/**
 * Profile fragment with user information and logout
 */
class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        loadUserInfo()
        setupClickListeners()
    }
    
    private fun loadUserInfo() {
        val tokenManager = (requireActivity().application as CarPartsApplication).tokenManager
        
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(requireContext())
            val user = database.userDao().getCurrentUser()
            
            user?.let {
                binding.tvUserName.text = it.fullName
                binding.tvUserEmail.text = it.email
                binding.tvUserPhone.text = it.phoneNumber ?: "Not provided"
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }
    
    private fun logout() {
        lifecycleScope.launch {
            val database = AppDatabase.getInstance(requireContext())
            val tokenManager = (requireActivity().application as CarPartsApplication).tokenManager
            val repository = AuthRepository(
                RetrofitClient.authService,
                database.userDao(),
                tokenManager
            )
            
            repository.logout()
            
            // Navigate to login
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
