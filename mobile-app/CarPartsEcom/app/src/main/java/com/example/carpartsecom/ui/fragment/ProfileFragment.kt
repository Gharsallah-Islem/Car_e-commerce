package com.example.carpartsecom.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.carpartsecom.MainActivity
import com.example.carpartsecom.R
import com.example.carpartsecom.ui.viewmodel.ProfileViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ProfileFragment : Fragment() {
    private lateinit var profileViewModel: ProfileViewModel
    
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var loadingProgress: CircularProgressIndicator
    private lateinit var errorCard: MaterialCardView
    private lateinit var errorText: TextView
    private lateinit var retryButton: MaterialButton
    
    private lateinit var accountCard: MaterialCardView
    private lateinit var personalCard: MaterialCardView
    private lateinit var securityCard: MaterialCardView
    
    private lateinit var profileName: TextView
    private lateinit var emailText: TextView
    private lateinit var createdAtText: TextView
    private lateinit var verifiedBadge: TextView
    
    private lateinit var firstNameLayout: TextInputLayout
    private lateinit var firstNameEdit: TextInputEditText
    private lateinit var lastNameLayout: TextInputLayout
    private lateinit var lastNameEdit: TextInputEditText
    private lateinit var phoneLayout: TextInputLayout
    private lateinit var phoneEdit: TextInputEditText
    
    private lateinit var updateButton: MaterialButton
    private lateinit var changePasswordButton: MaterialButton
    private lateinit var logoutButton: MaterialButton
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val factory = (requireActivity() as MainActivity).viewModelFactory
        profileViewModel = ViewModelProvider(requireActivity(), factory)[ProfileViewModel::class.java]
        
        // Initialize views
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        loadingProgress = view.findViewById(R.id.loadingProgress)
        errorCard = view.findViewById(R.id.errorCard)
        errorText = view.findViewById(R.id.errorText)
        retryButton = view.findViewById(R.id.retryButton)
        
        accountCard = view.findViewById(R.id.accountCard)
        personalCard = view.findViewById(R.id.personalCard)
        securityCard = view.findViewById(R.id.securityCard)
        
        profileName = view.findViewById(R.id.profileName)
        emailText = view.findViewById(R.id.emailText)
        createdAtText = view.findViewById(R.id.createdAtText)
        verifiedBadge = view.findViewById(R.id.verifiedBadge)
        
        firstNameLayout = view.findViewById(R.id.firstNameLayout)
        firstNameEdit = view.findViewById(R.id.firstNameEdit)
        lastNameLayout = view.findViewById(R.id.lastNameLayout)
        lastNameEdit = view.findViewById(R.id.lastNameEdit)
        phoneLayout = view.findViewById(R.id.phoneLayout)
        phoneEdit = view.findViewById(R.id.phoneEdit)
        
        updateButton = view.findViewById(R.id.updateProfileButton)
        changePasswordButton = view.findViewById(R.id.changePasswordButton)
        logoutButton = view.findViewById(R.id.logoutButton)
        
        // Setup pull-to-refresh
        swipeRefresh.setColorSchemeResources(R.color.primary)
        swipeRefresh.setOnRefreshListener {
            profileViewModel.loadProfile()
        }
        
        // Retry button
        retryButton.setOnClickListener {
            profileViewModel.loadProfile()
        }
        
        // Initial load
        showLoading()
        profileViewModel.loadProfile()
        
        // Observe profile data
        profileViewModel.profile.observe(viewLifecycleOwner) { result ->
            hideLoading()
            swipeRefresh.isRefreshing = false
            
            result.onSuccess { profile ->
                showContent()

                // Display name in header
                val fullName = listOfNotNull(profile.firstName, profile.lastName)
                    .joinToString(" ")
                    .ifEmpty { "User" }
                profileName.text = fullName

                emailText.text = profile.email
                createdAtText.text = formatDate(profile.createdAt)
                verifiedBadge.visibility = if (profile.verified) View.VISIBLE else View.GONE
                
                firstNameEdit.setText(profile.firstName ?: "")
                lastNameEdit.setText(profile.lastName ?: "")
                phoneEdit.setText(profile.phoneNumber?.removePrefix("+")?.trim() ?: "")
            }
            result.onFailure { error ->
                showError(error.message ?: "Failed to load profile")
            }
        }

        // Update profile button
        updateButton.setOnClickListener {
            val firstName = firstNameEdit.text.toString().trim()
            val lastName = lastNameEdit.text.toString().trim()
            val phone = phoneEdit.text.toString().trim()

            var isValid = true

            // First name validation
            if (firstName.isEmpty()) {
                firstNameLayout.error = "First name is required"
                isValid = false
            } else if (firstName.length < 2) {
                firstNameLayout.error = "First name must be at least 2 characters"
                isValid = false
            } else if (!firstName.matches(Regex("^[a-zA-Z\\s'-]+$"))) {
                firstNameLayout.error = "First name can only contain letters"
                isValid = false
            } else {
                firstNameLayout.error = null
            }

            // Last name validation
            if (lastName.isEmpty()) {
                lastNameLayout.error = "Last name is required"
                isValid = false
            } else if (lastName.length < 2) {
                lastNameLayout.error = "Last name must be at least 2 characters"
                isValid = false
            } else if (!lastName.matches(Regex("^[a-zA-Z\\s'-]+$"))) {
                lastNameLayout.error = "Last name can only contain letters"
                isValid = false
            } else {
                lastNameLayout.error = null
            }

            // Phone validation (optional but if provided must be valid)
            if (phone.isNotEmpty()) {
                val phoneDigits = phone.replace(Regex("[^0-9]"), "")
                if (phoneDigits.length < 8 || phoneDigits.length > 15) {
                    phoneLayout.error = "Please enter a valid phone number (8-15 digits)"
                    isValid = false
                } else if (!phone.matches(Regex("^[+]?[0-9\\s\\-()]+$"))) {
                    phoneLayout.error = "Phone can only contain digits, +, -, (), and spaces"
                    isValid = false
                } else {
                    phoneLayout.error = null
                }
            } else {
                phoneLayout.error = null
            }

            if (!isValid) return@setOnClickListener

            updateButton.isEnabled = false
            updateButton.text = "Updating..."
            profileViewModel.updateProfile(firstName, lastName, phone.ifEmpty { null })
        }

        // Observe update status
        profileViewModel.updateProfileStatus.observe(viewLifecycleOwner) { result ->
            updateButton.isEnabled = true
            updateButton.text = "Update Profile"

            result.onSuccess {
                showSnackbar("Profile updated successfully!")
                profileViewModel.loadProfile() // Reload to show updated data
            }
            result.onFailure { error ->
                showSnackbar("Update failed: ${error.message}")
            }
        }

        // Change password button
        changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }

        // Claims button - navigate to Claims screen
        view.findViewById<MaterialButton>(R.id.claimsButton)?.setOnClickListener {
            (requireActivity() as MainActivity).navigateToFragment(ClaimFragment())
        }

        // Logout button
        logoutButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    (requireActivity() as MainActivity).logout()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    
    private fun showLoading() {
        loadingProgress.visibility = View.VISIBLE
        errorCard.visibility = View.GONE
        accountCard.visibility = View.GONE
        personalCard.visibility = View.GONE
        securityCard.visibility = View.GONE
        view?.findViewById<MaterialCardView>(R.id.supportCard)?.visibility = View.GONE
        logoutButton.visibility = View.GONE
    }

    private fun hideLoading() {
        loadingProgress.visibility = View.GONE
    }

    private fun showContent() {
        errorCard.visibility = View.GONE
        accountCard.visibility = View.VISIBLE
        personalCard.visibility = View.VISIBLE
        securityCard.visibility = View.VISIBLE
        view?.findViewById<MaterialCardView>(R.id.supportCard)?.visibility = View.VISIBLE
        logoutButton.visibility = View.VISIBLE
    }
    
    private fun showError(message: String) {
        errorCard.visibility = View.VISIBLE
        errorText.text = message
        accountCard.visibility = View.GONE
        personalCard.visibility = View.GONE
        securityCard.visibility = View.GONE
        view?.findViewById<MaterialCardView>(R.id.supportCard)?.visibility = View.GONE
        logoutButton.visibility = View.GONE
    }
    
    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_change_password, null)

        val currentPasswordLayout = dialogView.findViewById<TextInputLayout>(R.id.currentPasswordLayout)
        val currentPasswordEdit = dialogView.findViewById<TextInputEditText>(R.id.currentPasswordEdit)
        val newPasswordLayout = dialogView.findViewById<TextInputLayout>(R.id.newPasswordLayout)
        val newPasswordEdit = dialogView.findViewById<TextInputEditText>(R.id.newPasswordEdit)
        val confirmPasswordLayout = dialogView.findViewById<TextInputLayout>(R.id.confirmPasswordLayout)
        val confirmPasswordEdit = dialogView.findViewById<TextInputEditText>(R.id.confirmPasswordEdit)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change", null) // Set to null initially to override later
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val currentPassword = currentPasswordEdit.text.toString()
                val newPassword = newPasswordEdit.text.toString()
                val confirmPassword = confirmPasswordEdit.text.toString()
                
                var isValid = true
                
                if (currentPassword.isEmpty()) {
                    currentPasswordLayout.error = "Current password is required"
                    isValid = false
                } else {
                    currentPasswordLayout.error = null
                }
                
                if (newPassword.isEmpty()) {
                    newPasswordLayout.error = "New password is required"
                    isValid = false
                } else if (newPassword.length < 6) {
                    newPasswordLayout.error = "Password must be at least 6 characters"
                    isValid = false
                } else {
                    newPasswordLayout.error = null
                }
                
                if (confirmPassword != newPassword) {
                    confirmPasswordLayout.error = "Passwords do not match"
                    isValid = false
                } else {
                    confirmPasswordLayout.error = null
                }
                
                if (isValid) {
                    positiveButton.isEnabled = false
                    positiveButton.text = "Changing..."
                    profileViewModel.changePassword(currentPassword, newPassword)
                }
            }
        }

        dialog.show()

        // Observe password change status - using a one-time observer
        profileViewModel.changePasswordStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                showSnackbar("Password changed successfully!")
                dialog.dismiss()
            }
            result.onFailure { error ->
                val errorMessage = error.message ?: "Failed to change password"
                // Check if it's an auth error
                if (errorMessage.contains("401") || errorMessage.contains("Unauthorized", ignoreCase = true)) {
                    currentPasswordLayout.error = "Current password is incorrect"
                } else {
                    showSnackbar("Failed: $errorMessage")
                }
                // Re-enable the button
                dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.let { btn ->
                    btn.isEnabled = true
                    btn.text = "Change"
                }
            }
        }
    }
    
    private fun formatDate(dateString: String): String {
        return try {
            val parts = dateString.split("T")[0].split("-")
            if (parts.size == 3) {
                val year = parts[0]
                val month = when(parts[1].toInt()) {
                    1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"
                    5 -> "May"; 6 -> "June"; 7 -> "July"; 8 -> "August"
                    9 -> "September"; 10 -> "October"; 11 -> "November"; 12 -> "December"
                    else -> parts[1]
                }
                val day = parts[2].toInt()
                "$month $day, $year"
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }
    
    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }
}
