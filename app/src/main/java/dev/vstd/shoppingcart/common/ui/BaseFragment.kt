package dev.vstd.shoppingcart.common.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<T : ViewBinding> : Fragment() {
    private var binding: T? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = viewCreator(inflater, container, false)
        return binding?.root
    }

    abstract fun onViewCreated(binding: T)

    abstract val viewCreator: (LayoutInflater, ViewGroup?, Boolean) -> T

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let { onViewCreated(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    fun getBinding(): T? {
        return binding
    }
}