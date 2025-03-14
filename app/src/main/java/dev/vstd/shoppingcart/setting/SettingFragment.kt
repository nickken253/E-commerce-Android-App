package dev.vstd.shoppingcart.setting

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dev.keego.shoppingcart.R
import dev.keego.shoppingcart.databinding.FragmentSettingBinding
import dev.vstd.shoppingcart.auth.Session
import dev.vstd.shoppingcart.common.ui.BaseFragment
import dev.vstd.shoppingcart.common.utils.toast
import kotlinx.coroutines.launch

class SettingFragment : BaseFragment<FragmentSettingBinding>() {
    override fun onViewCreated(binding: FragmentSettingBinding) {

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_settingFragment_to_personalInfoFragment)
        }
        binding.btnCurrency.setOnClickListener {
            requireContext().toast("This feature is not available yet")
        }
        binding.btnGithub.setOnClickListener {
            Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("https://github.com/inusedname/Curr-Mobile-App")
                startActivity(this)
            }
        }
        binding.btnShippingAddress.setOnClickListener {
            findNavController().navigate(R.id.action_settingFragment_to_updateAddressFragment)
        }
        binding.btnPaymentMethods.setOnClickListener {
            findNavController().navigate(R.id.action_settingFragment_to_nav_payment_method)
        }
        binding.btnLogout.setOnClickListener {
            Session.userEntity.value = null
            requireContext().toast("Logged out")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Session.userEntity.collect {
                    binding.btnShippingAddress.isEnabled = it != null
                    binding.btnPaymentMethods.isEnabled = it != null
                    binding.btnEditProfile.isEnabled = it != null
                    binding.btnLogout.isVisible = it != null
                    if (it == null) {
                        binding.tvUsername.text = "Not logged in"
                        binding.ivAvatar.setImageResource(R.drawable.ic_person)
                        binding.ivAvatar.setOnClickListener {
                            findNavController().navigate(R.id.action_settingFragment_to_authActivity)
                        }
                    } else {
                        binding.tvUsername.text = it.username
                        binding.ivAvatar.setImageResource(R.drawable.img_person)
                    }
                }
            }
        }
    }

    override val viewCreator: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSettingBinding
        get() = FragmentSettingBinding::inflate
}