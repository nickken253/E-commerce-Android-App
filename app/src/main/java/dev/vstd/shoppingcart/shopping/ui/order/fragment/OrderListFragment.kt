package dev.vstd.shoppingcart.shopping.ui.order.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.keego.shoppingcart.databinding.FragmentOrderListBinding
import dev.vstd.shoppingcart.common.UiStatus
import dev.vstd.shoppingcart.common.ui.BaseFragment
import dev.vstd.shoppingcart.common.utils.beGone
import dev.vstd.shoppingcart.common.utils.beVisible
import dev.vstd.shoppingcart.shopping.data.repository.OrderRepository
import dev.vstd.shoppingcart.shopping.data.service.OrderRespDto
import dev.vstd.shoppingcart.shopping.domain.Order
import dev.vstd.shoppingcart.shopping.domain.Status
import dev.vstd.shoppingcart.shopping.ui.order.adapter.OrderAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OrderListFragment(private val status: Status) : BaseFragment<FragmentOrderListBinding>() {
    @Inject
    lateinit var repository: OrderRepository
    private val uiStatus = MutableStateFlow<UiStatus<List<Order>>>(UiStatus.Loading())

    private val adapter by lazy {
        OrderAdapter()
    }

    override fun onViewCreated(binding: FragmentOrderListBinding) {
        binding.rvOrders.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            val resp = repository.getAllOrders()
            if (resp.isSuccessful) {
                val orders = resp.body()!!.filter { it.status == status }.map(OrderRespDto::toOrder)
                uiStatus.value = UiStatus.Success(orders)
            } else {
                uiStatus.value = UiStatus.Error(resp.errorBody()?.string() ?: "Please try again later")
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                uiStatus.collect {
                    when (it) {
                        is UiStatus.Loading -> {
                            binding.layoutUiLoading.root.beVisible()
                            binding.layoutUiError.root.beGone()
                        }
                        is UiStatus.Success -> {
                            binding.layoutUiLoading.root.beGone()
                            val orders = it.data
                            if (orders.isEmpty()) {
                                binding.layoutUiError.apply {
                                    root.beVisible()
                                    tvError.text = "No orders found"
                                }
                            } else {
                                adapter.submitListt(orders)
                            }
                        }
                        is UiStatus.Error -> {
                            binding.layoutUiLoading.root.beGone()
                            binding.layoutUiError.apply {
                                root.beVisible()
                                tvError.text = it.message
                            }
                        }

                        is UiStatus.Initial -> {}
                    }
                }
            }
        }
    }

    override val viewCreator: (LayoutInflater, ViewGroup?, Boolean) -> FragmentOrderListBinding
        get() = FragmentOrderListBinding::inflate
}