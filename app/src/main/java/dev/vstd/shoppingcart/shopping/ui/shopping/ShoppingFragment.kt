package dev.vstd.shoppingcart.shopping.ui.shopping

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dev.keego.shoppingcart.R
import dev.keego.shoppingcart.databinding.FragmentShoppingBinding
import dev.vstd.shoppingcart.auth.Session
import dev.vstd.shoppingcart.common.ui.BaseFragment
import kotlinx.coroutines.launch

class ShoppingFragment: BaseFragment<FragmentShoppingBinding>() {
    override fun onViewCreated(binding: FragmentShoppingBinding) {
        setOnClicks(binding)
        observeStates(binding)
    }

    private fun observeStates(binding: FragmentShoppingBinding) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Session.userEntity.collect {
                    binding.apply {
                        if (it != null) {
                            greetings.text = "Good afternoon, ${it.username}!"
                            Glide.with(avatar)
                                .load(R.drawable.img_person)
                                .into(avatar)
                        } else {
                            greetings.text = "Good afternoon, Guest!"
                            Glide.with(avatar)
                                .load(R.drawable.ic_person)
                                .into(avatar)
                        }
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Session.userEntity.collect {
                    when (it) {
                        null -> {
                            binding.apply {
                                btnCart.isEnabled = false
                                btnOrders.isEnabled = false
                                btnLoginLogout.text = getString(R.string.login)
                                btnLoginLogout.setOnClickListener {
                                    findNavController().navigate(R.id.action_shoppingFragment_to_authActivity)
                                }
                            }
                        }
                        else -> {
                            binding.apply {
                                btnCart.isEnabled = true
                                btnOrders.isEnabled = true
                                btnLoginLogout.text = getString(R.string.logout)
                                btnLoginLogout.setOnClickListener {
                                    Session.userEntity.value = null
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setOnClicks(binding: FragmentShoppingBinding) {
        binding.btnCart.setOnClickListener {
            findNavController().navigate(R.id.action_shoppingFragment_to_paymentActivity)
        }
        binding.btnOrders.setOnClickListener {
            findNavController().navigate(R.id.action_shoppingFragment_to_ordersFragment)
        }
    }

    override val viewCreator: (LayoutInflater, ViewGroup?, Boolean) -> FragmentShoppingBinding
        get() = FragmentShoppingBinding::inflate
}