package com.integmobile.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android:view.ViewGroup
import androidx.fragment.app.Fragment
import com.integmobile.databinding.FragmentPaymentMethodBinding

/**
 * Payment method fragment - placeholder for payment selection
 */
class PaymentMethodFragment : Fragment() {
    
    private var _binding: FragmentPaymentMethodBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentMethodBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
