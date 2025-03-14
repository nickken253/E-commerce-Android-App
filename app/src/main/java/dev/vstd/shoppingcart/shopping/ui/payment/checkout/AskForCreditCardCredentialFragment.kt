package dev.vstd.shoppingcart.shopping.ui.payment.checkout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.keego.shoppingcart.R
import dev.keego.shoppingcart.databinding.FragmentAskForCreditCardCredentialBinding
import dev.vstd.shoppingcart.common.ui.BaseFragment
import dev.vstd.shoppingcart.common.utils.toast
import dev.vstd.shoppingcart.shopping.data.repository.CardRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AskForCreditCardCredentialFragment :
    BaseFragment<FragmentAskForCreditCardCredentialBinding>() {
    @Inject
    lateinit var cardRepository: CardRepository

    private var orderId: Long? = null

    override fun onViewCreated(binding: FragmentAskForCreditCardCredentialBinding) {
        if (arguments?.getLong("orderId") == null) {
            requireContext().toast("Some fatal error occurred. Please try again later")
            findNavController().popBackStack(R.id.shoppingFragment, false)
        }
        arguments?.getLong("orderId")?.let {
            orderId = it
        }

        binding.etCreditCardCVV.addTextChangedListener { editable ->
            binding.btnNext.isEnabled = editable!!.isNotEmpty() && editable.length == 3
            binding.btnNext.setOnClickListener {
                lifecycleScope.launch {
                    try {
                        val cvv = binding.etCreditCardCVV.text.toString()
                        val resp = cardRepository.payByCard(orderId!!, cvv)
                        if (resp.isSuccessful) {
                            requireContext().toast("Payment success!")
                            requireActivity().finish()
                        } else {
                            requireContext().toast(resp.errorBody()!!.string())
                        }
                    } catch (e: Exception) {
                        Timber.e(e)
                        requireContext().toast("Some error occurred. Please try again later")
                    }
                }
            }
        }
    }

    override val viewCreator: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAskForCreditCardCredentialBinding
        get() = FragmentAskForCreditCardCredentialBinding::inflate
}