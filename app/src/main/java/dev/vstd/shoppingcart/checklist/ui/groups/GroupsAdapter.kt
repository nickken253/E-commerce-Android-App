package dev.vstd.shoppingcart.checklist.ui.groups

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import dev.keego.shoppingcart.databinding.ItemTodoBinding
import dev.keego.shoppingcart.databinding.LayoutTextViewBinding
import dev.vstd.shoppingcart.checklist.data.TodoItem
import dev.vstd.shoppingcart.checklist.domain.GroupWithTodos
import dev.vstd.shoppingcart.common.ui.DiffUtils
import java.time.format.DateTimeFormatter
import java.util.Locale

class GroupsAdapter(private val onCheckClick: (TodoItem) -> Unit):
    ListAdapter<GroupsAdapter.DataWrapper, GroupsAdapter.ViewHolderWrapper>(
    DiffUtils.any()
) {
    fun submitListt(data: List<GroupWithTodos>) {
        val list = mutableListOf<DataWrapper>()
        data.forEach { group ->
            list.add(DataWrapper.Title(group.group.id, group.group.title))
            group.todos.forEach { item ->
                list.add(DataWrapper.CheckableItem(item))
            }
        }
        submitList(list)
    }
    sealed class DataWrapper {
        data class Title(val cateId: Long, val title: String) : DataWrapper()
        data class CheckableItem(val item: TodoItem) : DataWrapper()
    }

    sealed class ViewHolderWrapper(binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        class TitleViewHolder(private val binding: LayoutTextViewBinding) :
            ViewHolderWrapper(binding) {
            fun bind(cateId: Long, title: String) {
                binding.textView.text = title
            }
        }

        class CheckableItemViewHolder(private val onCheckClick: (TodoItem) -> Unit, val binding: ItemTodoBinding) :
            ViewHolderWrapper(binding) {
            fun bind(item: TodoItem) {
                binding.title.text = item.title
                binding.checkBox.isActivated = item.isCompleted
                binding.checkBox.setOnClickListener {
                    onCheckClick(item)
                }
                binding.tvTime.text = item.createdTime.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))

                val disabledColor = binding.root.context.getColor(android.R.color.darker_gray)
                when (item.isCompleted) {
                    true -> {
                        binding.title.setTextColor(disabledColor)
                        binding.tvTime.setTextColor(disabledColor)
                        binding.title.paint.isStrikeThruText = true
                        binding.tvTime.paint.isStrikeThruText = true
                    }
                    false -> {
                        binding.title.setTextColor(Color.BLACK)
                        binding.tvTime.setTextColor(Color.BLACK)
                        binding.title.paint.isStrikeThruText = false
                        binding.tvTime.paint.isStrikeThruText = false
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderWrapper {
        return when (viewType) {
            1 -> ViewHolderWrapper.TitleViewHolder(
                LayoutTextViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            2 -> ViewHolderWrapper.CheckableItemViewHolder(
                onCheckClick = onCheckClick,
                ItemTodoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position]) {
            is DataWrapper.Title -> 1
            is DataWrapper.CheckableItem -> 2
        }
    }

    override fun onBindViewHolder(holder: ViewHolderWrapper, position: Int) {
        when (val item = currentList[position]) {
            is DataWrapper.Title -> {
                (holder as ViewHolderWrapper.TitleViewHolder).bind(item.cateId, item.title)
            }
            is DataWrapper.CheckableItem -> {
                (holder as ViewHolderWrapper.CheckableItemViewHolder).bind(item.item)
            }
        }
    }
}