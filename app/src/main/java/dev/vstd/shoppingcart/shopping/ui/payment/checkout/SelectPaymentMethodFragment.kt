package dev.vstd.shoppingcart.shopping.ui.payment.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.keego.shoppingcart.databinding.FragmentSelectPaymentMethodBinding
import dev.vstd.shoppingcart.common.ui.BaseFragment
import dev.vstd.shoppingcart.shopping.data.repository.CardRepository
import dev.vstd.shoppingcart.shopping.domain.PaymentMethod
import dev.vstd.shoppingcart.shopping.ui.shopping.PaymentMethodsAdapter
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SelectPaymentMethodFragment : BaseFragment<FragmentSelectPaymentMethodBinding>() {

    @Inject
    lateinit var cardRepository: CardRepository

    override fun onViewCreated(binding: FragmentSelectPaymentMethodBinding) {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.rvPaymentMethods.adapter = PaymentMethodsAdapter {
            if (it.type == PaymentMethod.Type.MOMO) {
                Toast.makeText(requireContext(), "Momo is not supported yet", Toast.LENGTH_SHORT)
                    .show()
                return@PaymentMethodsAdapter
            }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Payment Method")
                .setMessage("Are you sure you want to select ${it.type.title} as Payment Method?")
                .setPositiveButton("Yes") { _, _ ->
                    val bundle = Bundle().apply {
                        putSerializable(EXTRA_PAYMENT_METHOD_SERIALIZE, it)
                    }
                    setFragmentResult(RESULT_OK, bundle)
                    findNavController().popBackStack()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val methods = PaymentMethod.getDefaultOptions().toMutableList()
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
            (binding.rvPaymentMethods.adapter as PaymentMethodsAdapter).submitList(
                methods
            )
        }

    }

    override val viewCreator: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSelectPaymentMethodBinding
        get() = FragmentSelectPaymentMethodBinding::inflate

    companion object {
        const val RESULT_OK = "SelectPaymentMethodFragment.RESULT_OK"
        const val EXTRA_PAYMENT_METHOD_SERIALIZE = "SelectPaymentMethodFragment.EXTRA_PAYMENT_METHOD_SERIALIZE"
    }
}