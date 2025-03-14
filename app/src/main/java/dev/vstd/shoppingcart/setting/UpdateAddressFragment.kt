package dev.vstd.shoppingcart.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.keego.shoppingcart.databinding.BotSheetRecyclerViewBinding
import dev.keego.shoppingcart.databinding.FragmentUpdateAddressBinding
import dev.vstd.shoppingcart.auth.Session
import dev.vstd.shoppingcart.auth.service.UserService
import dev.vstd.shoppingcart.common.ui.BaseFragment
import dev.vstd.shoppingcart.common.utils.VietnamAdministrationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


/**
 * Get result by using
 *
 * RESULT_OK -> bundle.getString(EXTRA_FULL_ADDRESS)
 * */
@AndroidEntryPoint
class UpdateAddressFragment : BaseFragment<FragmentUpdateAddressBinding>() {
    private lateinit var administrationProvider: VietnamAdministrationProvider
    private val city = MutableStateFlow<String?>(null)
    private val district = MutableStateFlow<String?>(null)

    @Inject
    lateinit var userService: UserService

    override fun onViewCreated(binding: FragmentUpdateAddressBinding) {
        administrationProvider = VietnamAdministrationProvider(requireContext())
        setOnClicks(binding)
        observeState(binding)
    }

    override fun onStart() {
        super.onStart()
        viewLifecycleOwner.lifecycleScope.launch {
            userService.getUserInfo(Session.userEntity.value!!.id).let {
                getBinding()?.apply {
                    tvOldAddress.text = it?.address
                }
            }
        }
    }

    private fun observeState(binding: FragmentUpdateAddressBinding) {
        lifecycleScope.launch {
            launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    city.collect {
                        binding.city.setText(it)
                        binding.districtWrapper.isEnabled = it != null
                    }
                }
            }
            launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    district.collect {
                        binding.district.setText(it)
                        binding.addressWrapper.isEnabled = it != null
                    }
                }
            }
        }
    }

    private fun setOnClicks(binding: FragmentUpdateAddressBinding) {
        binding.cityWrapper.isClickable = false
        binding.districtWrapper.isClickable = false
        binding.city.setOnClickListener {
            Timber.d("city clicked")
            val cityBotSheet = RVBotSheet(
                title = "Chọn Thành Phố",
                items = administrationProvider.getCities(),
                onSubmit = { city.value = it }
            )
            cityBotSheet.show(childFragmentManager, "cityBotSheet")
        }

        binding.district.setOnClickListener {
            if (city.value == null) return@setOnClickListener
            val districts = administrationProvider.getDistricts(city.value!!)
            if (districts == null) return@setOnClickListener

            val districtBotSheet = RVBotSheet(
                title = "Chọn Quận/Huyện",
                items = districts,
                onSubmit = { district.value = it }
            )
            districtBotSheet.show(childFragmentManager, "districtBotSheet")
        }

        binding.btnSave.setOnClickListener {
            val result = AddressFormValidator.validate(
                city.value,
                district.value,
                binding.address.text.toString()
            )
            if (!result.success) {
                Toast.makeText(context, result.errorMessage, Toast.LENGTH_SHORT).show()
            } else {
                setFragmentResult(
                    RESULT_OK,
                    bundleOf(
                        EXTRA_FULL_ADDRESS to "${city.value}, ${district.value}, ${binding.address.text}"
                    )
                )
                findNavController().navigateUp()
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override val viewCreator: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUpdateAddressBinding
        get() = FragmentUpdateAddressBinding::inflate

    class RVBotSheet(
        private val title: String,
        private val items: List<String>,
        private val onSubmit: (String) -> Unit,
    ) : BottomSheetDialogFragment() {
        private var binding: BotSheetRecyclerViewBinding? = null

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View {
            binding = BotSheetRecyclerViewBinding.inflate(inflater, container, false)
            return binding!!.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            binding?.apply {
                title.text = this@RVBotSheet.title
                wheelPicker.data = items
                btnSubmit.setOnClickListener {
                    onSubmit(items[wheelPicker.currentItemPosition])
                    dismiss()
                }
            }
        }
    }

    companion object {
        const val RESULT_OK = "UpdateAddressFragment.RESULT_OK"
        const val EXTRA_FULL_ADDRESS = "EXTRA_FULL_ADDRESS"
    }
}