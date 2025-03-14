package dev.vstd.shoppingcart.shopping.ui.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import dev.keego.shoppingcart.databinding.FragmentOrdersBinding
import dev.vstd.shoppingcart.common.ui.BaseFragment
import dev.vstd.shoppingcart.shopping.domain.Status
import dev.vstd.shoppingcart.shopping.ui.order.adapter.FragmentPageAdapter

class OrdersFragment : BaseFragment<FragmentOrdersBinding>() {
    private lateinit var adapter: FragmentPageAdapter

    override fun onViewCreated(binding: FragmentOrdersBinding) {
        initViews(binding)
    }

    private fun initViews(binding: FragmentOrdersBinding) {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        adapter = FragmentPageAdapter(childFragmentManager, lifecycle)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when(position) {
                0 -> tab.text = Status.PENDING.displayName
                1 -> tab.text = Status.DELIVERED.displayName
                2 -> tab.text = Status.SHIPPED.displayName
                3 -> tab.text = Status.CANCELLED.displayName
            }
        }.attach()
    }

    override val viewCreator: (LayoutInflater, ViewGroup?, Boolean) -> FragmentOrdersBinding
        get() = FragmentOrdersBinding::inflate
}