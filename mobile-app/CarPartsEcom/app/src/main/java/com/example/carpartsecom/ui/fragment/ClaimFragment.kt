package com.example.carpartsecom.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.carpartsecom.MainActivity
import com.example.carpartsecom.R
import com.example.carpartsecom.ui.viewmodel.ClaimViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ClaimFragment : Fragment() {
    private lateinit var claimViewModel: ClaimViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_claim, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = (requireActivity() as MainActivity).viewModelFactory
        claimViewModel = ViewModelProvider(requireActivity(), factory)[ClaimViewModel::class.java]

        val orderIdLayout = view.findViewById<TextInputLayout>(R.id.orderIdLayout)
        val orderIdEdit = view.findViewById<TextInputEditText>(R.id.orderIdEdit)
        val subjectLayout = view.findViewById<TextInputLayout>(R.id.subjectLayout)
        val subjectEdit = view.findViewById<TextInputEditText>(R.id.subjectEdit)
        val descriptionLayout = view.findViewById<TextInputLayout>(R.id.descriptionLayout)
        val descriptionEdit = view.findViewById<TextInputEditText>(R.id.descriptionEdit)
        val submitButton = view.findViewById<MaterialButton>(R.id.submitClaimButton)
        val claimsListText = view.findViewById<TextView>(R.id.claimsListText)

        submitButton.setOnClickListener {
            val orderIdStr = orderIdEdit.text.toString().trim()
            val subject = subjectEdit.text.toString().trim()
            val description = descriptionEdit.text.toString().trim()

            var isValid = true

            // Order ID validation
            val orderId = orderIdStr.toLongOrNull()
            if (orderIdStr.isEmpty()) {
                orderIdLayout?.error = "Order ID is required"
                isValid = false
            } else if (orderId == null || orderId <= 0) {
                orderIdLayout?.error = "Please enter a valid order ID (positive number)"
                isValid = false
            } else {
                orderIdLayout?.error = null
            }

            // Subject validation
            if (subject.isEmpty()) {
                subjectLayout?.error = "Subject is required"
                isValid = false
            } else if (subject.length < 5) {
                subjectLayout?.error = "Subject must be at least 5 characters"
                isValid = false
            } else if (subject.length > 100) {
                subjectLayout?.error = "Subject must be less than 100 characters"
                isValid = false
            } else {
                subjectLayout?.error = null
            }

            // Description validation
            if (description.isEmpty()) {
                descriptionLayout?.error = "Description is required"
                isValid = false
            } else if (description.length < 20) {
                descriptionLayout?.error = "Please provide more details (at least 20 characters)"
                isValid = false
            } else if (description.length > 1000) {
                descriptionLayout?.error = "Description must be less than 1000 characters"
                isValid = false
            } else {
                descriptionLayout?.error = null
            }

            if (!isValid) return@setOnClickListener

            submitButton.isEnabled = false
            submitButton.text = "Submitting..."
            claimViewModel.createClaim(orderId!!, subject, description)
        }

        claimViewModel.createClaimStatus.observe(viewLifecycleOwner) { result ->
            submitButton.isEnabled = true
            submitButton.text = "Submit Claim"

            result.onSuccess {
                Toast.makeText(context, "Claim submitted successfully", Toast.LENGTH_SHORT).show()
                // Clear fields
                orderIdEdit.text?.clear()
                subjectEdit.text?.clear()
                descriptionEdit.text?.clear()
            }
            result.onFailure {
                Toast.makeText(context, "Failed to submit claim: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        claimViewModel.claims.observe(viewLifecycleOwner) { claims ->
            if (claims.isEmpty()) {
                claimsListText.text = "No claims yet."
            } else {
                val sb = StringBuilder()
                claims.forEach { claim ->
                    sb.append("Claim #${claim.id}: ${claim.subject} (${claim.status})\n\n")
                }
                claimsListText.text = sb.toString()
            }
        }
    }
}
