package dev.vstd.shoppingcart.checklist.ui.addItem

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.keego.shoppingcart.databinding.ActivityAddItemBinding
import dev.vstd.shoppingcart.checklist.data.TodoGroup
import dev.vstd.shoppingcart.checklist.data.TodoItem
import dev.vstd.shoppingcart.checklist.data.TodoRepository
import dev.vstd.shoppingcart.checklist.ui.barcode.BarcodeActivity
import dev.vstd.shoppingcart.checklist.utils.dialogs.EditTextAlertDialog
import dev.vstd.shoppingcart.common.utils.toast
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class AddItemActivity : AppCompatActivity() {

    @Inject
    lateinit var todoRepository: TodoRepository

    private lateinit var binding: ActivityAddItemBinding

    private val barcodeActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == BarcodeActivity.RESULT_OK) {
                val barcode = result.data?.getStringExtra(BarcodeActivity.EXTRA_BARCODE_DESCRIPTION)
                barcode?.let {
                    binding.edtName.setText(it)
                }
            }
        }

    private suspend fun fetchGroups() {
        val groups = todoRepository.getAllGroups()
        val options = mutableListOf("Create new group...")
        options.addAll(groups.map { it.title })
        binding.dropdownGroups.setSimpleItems(options.toTypedArray())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initViews()

        lifecycleScope.launch {
            fetchGroups()
        }

        binding.btnScan.setOnClickListener {
            barcodeActivityLauncher.launch(Intent(this, BarcodeActivity::class.java))
        }

        binding.dropdownGroups.setText("Select Group...")

        binding.btnAdd.setOnClickListener {
            val title = binding.edtName.text.toString()
            val group = binding.dropdownGroups.text.toString()

            if (title.isBlank()) {
                toast("Title cannot be empty")
                return@setOnClickListener
            }

            if (group == "Create new group..." || group == "Select Group...") {
                toast("Please select a group")
                return@setOnClickListener
            }
            lifecycleScope.launch {
                todoRepository.insertTodo(
                    TodoItem(
                        title = title,
                        groupId = todoRepository.findGroupByTitle(group)!!.id,
                        createdTime = LocalDateTime.now()
                    )
                )
                finish()
            }
        }

        binding.dropdownGroups.setOnItemClickListener { adapterView, _, position, _ ->
            if (position == 0) {
                EditTextAlertDialog.create(
                    this,"Create new group", onCreateClicked = {
                        lifecycleScope.launch {
                            todoRepository.insertGroup(TodoGroup(title = it))
                            fetchGroups()
                        }
                    }
                ).show()
                binding.dropdownGroups.setSelection(0)
            }
        }
    }

    private fun initViews() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun initBinding() {
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    companion object {
        fun start(context: Context) {
            Intent(context, AddItemActivity::class.java).also {
                context.startActivity(it)
            }
        }
    }
}