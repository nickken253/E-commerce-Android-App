package dev.vstd.shoppingcart.checklist.ui.groups

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.keego.shoppingcart.databinding.FragmentGroupsBinding
import dev.vstd.shoppingcart.checklist.domain.GroupWithTodos
import dev.vstd.shoppingcart.checklist.ui.addItem.AddItemActivity
import dev.vstd.shoppingcart.common.UiStatus
import dev.vstd.shoppingcart.common.ui.BaseFragment
import dev.vstd.shoppingcart.common.utils.beGone
import dev.vstd.shoppingcart.common.utils.beVisible
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GroupsFragment : BaseFragment<FragmentGroupsBinding>() {
    private val vimel by viewModels<GroupsVimel>()
    private val uiState = MutableStateFlow<UiStatus<List<GroupWithTodos>>>(UiStatus.Loading())

    override fun onViewCreated(binding: FragmentGroupsBinding) {
        binding.fabAddNewTodo.setOnClickListener {
            AddItemActivity.start(requireContext())
        }
        val adapter = GroupsAdapter {
            vimel.toggleDoneUndone(it)
        }.also { binding.rvGroups.adapter = it }

        observeData(binding, adapter)
    }

    override fun onStart() {
        super.onStart()
        vimel.fetch()
    }

    private fun observeData(binding: FragmentGroupsBinding, adapter: GroupsAdapter) {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    vimel.groupsWithTodos.collectLatest {
                        uiState.value = UiStatus.Success(it)
                    }
                }
            }
            launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    uiState.collectLatest {
                        when (it) {
                            is UiStatus.Error -> {
                                binding.layoutError.root.beVisible()
                                binding.layoutLoading.root.beGone()
                                binding.layoutEmpty.root.beGone()
                                binding.rvGroups.beGone()
                                binding.layoutError.tvError.text = it.message
                            }

                            is UiStatus.Initial -> {

                            }
                            is UiStatus.Loading -> {
                                binding.rvGroups.beGone()
                                binding.layoutLoading.root.beVisible()
                            }

                            is UiStatus.Success -> {
                                if (it.data.isEmpty()) {
                                    binding.layoutLoading.root.beGone()
                                    binding.layoutEmpty.root.beVisible()
                                } else {
                                    binding.layoutLoading.root.beGone()
                                    binding.layoutEmpty.root.beGone()
                                    binding.rvGroups.beVisible()
                                    adapter.submitListt(it.data)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override val viewCreator: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGroupsBinding
        get() = FragmentGroupsBinding::inflate
}