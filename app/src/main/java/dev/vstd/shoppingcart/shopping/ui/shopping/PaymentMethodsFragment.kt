package dev.vstd.shoppingcart.shopping.ui.shopping

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.keego.shoppingcart.R
import dev.keego.shoppingcart.databinding.FragmentPaymentMethodsBinding
import dev.vstd.shoppingcart.auth.data.UserRepository
import dev.vstd.shoppingcart.common.ui.BaseFragment
import dev.vstd.shoppingcart.common.utils.beGone
import dev.vstd.shoppingcart.common.utils.beVisible
import dev.vstd.shoppingcart.shopping.data.repository.CardRepository
import dev.vstd.shoppingcart.shopping.domain.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PaymentMethodsFragment : BaseFragment<FragmentPaymentMethodsBinding>() {
    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var cardRepository: CardRepository

    private val cards = MutableStateFlow(listOf<PaymentMethod>())

    override fun onViewCreated(binding: FragmentPaymentMethodsBinding) {
        binding.sectionAddNewCard.apply {
            purchaseName.text = "Register your new card now"
            purchaseDesc.text = "Click here!"
            purchaseIcon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_add_card,
                    null
                )
            )
            root.setOnClickListener {
                findNavController().navigate(R.id.action_paymentMethodsFragment_to_registerCardFragment)
            }
            root.beGone() // hide it
        }

        setOnClicks(binding)
        observeState(binding)

        fetchData(binding)
    }

    private fun fetchData(binding: FragmentPaymentMethodsBinding) {
        viewLifecycleOwner.lifecycleScope.launch {
            val methods = PaymentMethod.getDefaultOptions().dropLast(1).toMutableList()
            val response = cardRepository.getCard()
            if (response.isSuccessful) {
                val item = response.body()!!
                methods.add(
                    PaymentMethod(
                        item.id,
                        type = PaymentMethod.Type.CREDIT_CARD,
                        "**** **** **** ${item.cardNumber.takeLast(4)}",
                        balance = item.balance
                    )
                )
            }
            if (methods.size == PaymentMethod.getDefaultOptions().size - 1) {
                binding.sectionAddNewCard.root.beVisible()
            }
            cards.value = methods
        }
    }

    private fun observeState(binding: FragmentPaymentMethodsBinding) {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    cards.collect {
                        (binding.rvAccountsAndCards.adapter as PaymentMethodsAdapter).submitList(it)
                    }
                }
            }
        }
    }

    private fun setOnClicks(binding: FragmentPaymentMethodsBinding) {
        binding.rvAccountsAndCards.adapter = PaymentMethodsAdapter {

        }
        binding.btnShowBalance.setOnClickListener {
            (binding.rvAccountsAndCards.adapter as PaymentMethodsAdapter).toggleBalance()
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override val viewCreator: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPaymentMethodsBinding
        get() = FragmentPaymentMethodsBinding::inflate

    companion object {

    }
}