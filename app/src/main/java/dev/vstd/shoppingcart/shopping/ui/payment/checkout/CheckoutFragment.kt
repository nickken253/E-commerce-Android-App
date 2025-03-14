package dev.vstd.shoppingcart.shopping.ui.payment.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import dev.vstd.shoppingcart.common.theme.ShoppingCartTheme
import dev.vstd.shoppingcart.setting.UpdateAddressFragment
import dev.vstd.shoppingcart.shopping.domain.PaymentMethod

class CheckoutFragment : Fragment() {
    private val vimel by activityViewModels<CheckoutVimel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setFragmentResultListener(UpdateAddressFragment.RESULT_OK) { _, bundle ->
            val address = bundle.getString(UpdateAddressFragment.EXTRA_FULL_ADDRESS)
            address?.let { vimel.setAddress(it) }
        }

        setFragmentResultListener(SelectPaymentMethodFragment.RESULT_OK) { _, bundle ->
            val paymentMethod = bundle.getSerializable(SelectPaymentMethodFragment.EXTRA_PAYMENT_METHOD_SERIALIZE) as PaymentMethod?
            paymentMethod?.let { vimel.setPaymentMethod(it) }
        }

        return ComposeView(requireContext()).apply {
            setContent {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    ShoppingCartTheme {
                        checkout_(vimel = vimel, navController = findNavController()) {
                            activity?.finish()
                        }
                    }
                }
            }
        }
    }
}