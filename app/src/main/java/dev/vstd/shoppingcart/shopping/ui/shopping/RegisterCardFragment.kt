package dev.vstd.shoppingcart.shopping.ui.shopping

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.keego.shoppingcart.databinding.FragmentRegisterCardBinding
import dev.vstd.shoppingcart.common.ui.BaseFragment
import dev.vstd.shoppingcart.common.utils.toast
import dev.vstd.shoppingcart.shopping.data.repository.CardRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RegisterCardFragment : BaseFragment<FragmentRegisterCardBinding>() {
    @Inject
    lateinit var cardRepository: CardRepository

    override fun onViewCreated(binding: FragmentRegisterCardBinding) {
        viewLifecycleOwner.lifecycleScope.launch {
            val resp = cardRepository.registerCard()
            if (resp.isSuccessful) {
                val card = resp.body()!!
                binding.apply {
                    tvCVV.text = "CVV ${card.cvv}"
                    tvCardNumber.text = card.cardNumber
                    tvExpirationDate.text = "Exp ${card.expirationDate}"
                }
            }
        }
        binding.checkboxConfirm.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> binding.btnRegisterCard.isEnabled = true
                false -> binding.btnRegisterCard.isEnabled = false
            }
        }
        binding.checkboxConfirm.isChecked = false
        binding.btnRegisterCard.setOnClickListener {
            requireContext().toast("Card Register Success!")
            findNavController().navigateUp()
        }
    }

    override val viewCreator: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRegisterCardBinding
        get() = FragmentRegisterCardBinding::inflate
}