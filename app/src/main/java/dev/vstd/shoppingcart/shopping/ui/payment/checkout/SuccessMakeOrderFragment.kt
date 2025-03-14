package dev.vstd.shoppingcart.shopping.ui.payment.checkout

import android.view.LayoutInflater
import android.view.ViewGroup
import dev.keego.shoppingcart.databinding.FragmentSuccessMakeOrderBinding
import dev.vstd.shoppingcart.common.ui.BaseFragment
import dev.vstd.shoppingcart.shopping.ui.payment.PaymentActivity

class SuccessMakeOrderFragment: BaseFragment<FragmentSuccessMakeOrderBinding>() {
    override fun onViewCreated(binding: FragmentSuccessMakeOrderBinding) {
        binding.btnBackToHome.setOnClickListener {
            (requireActivity() as PaymentActivity).finish()
        }
    }

    override val viewCreator: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSuccessMakeOrderBinding
        get() = FragmentSuccessMakeOrderBinding::inflate
}