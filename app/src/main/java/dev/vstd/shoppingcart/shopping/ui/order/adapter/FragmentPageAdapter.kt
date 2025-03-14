package dev.vstd.shoppingcart.shopping.ui.order.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import dev.vstd.shoppingcart.shopping.domain.Status
import dev.vstd.shoppingcart.shopping.ui.order.fragment.OrderListFragment

class FragmentPageAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return Status.entries.size
    }

    override fun createFragment(position: Int): Fragment {

        val status = when (position) {
            0 -> Status.PENDING
            1 -> Status.DELIVERED
            2 -> Status.SHIPPED
            3 -> Status.CANCELLED
            else -> throw IllegalArgumentException("Invalid position")
        }
        return OrderListFragment(status)
    }
}