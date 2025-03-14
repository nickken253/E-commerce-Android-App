package dev.vstd.shoppingcart.checklist.utils.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.keego.shoppingcart.databinding.LayoutTextInputBinding
import dev.vstd.shoppingcart.common.utils.beGone

object EditTextAlertDialog {
    @Suppress("LocalVariableName")
    fun create(
        _context: Context,
        dialogTitle: String,
        scanable: Boolean = false,
        editTextValue: String? = null,
        onBarcodeIconClick: () -> Unit = {},
        onCreateClicked: (String) -> Unit,
    ): AlertDialog {
        val builder = MaterialAlertDialogBuilder(_context)
        val context = builder.context
        val binding = LayoutTextInputBinding.inflate(LayoutInflater.from(context))

        if (!scanable) binding.btnScan.beGone()
        if (editTextValue != null) binding.etName.setText(editTextValue)

        binding.btnScan.setOnClickListener {
            onBarcodeIconClick()
        }

        builder
            .setTitle(dialogTitle)
            .setPositiveButton("Create") { _, _ ->
                // Create new group
                val name = binding.etName.text.toString()
                if (name.isNotBlank()) {
                    onCreateClicked(name)
                } else {
                    // Show error
                    Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .setView(binding.root)

        return builder.create()
    }
}