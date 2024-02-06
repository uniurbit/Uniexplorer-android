package com.appload.einkapp.view.common

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.appload.einkapp.databinding.DialogConfirmationBinding

class ConfirmationDialog(
    context: Context,
    val title: String,
    private val description: String,
    val result: (Boolean) -> Unit
) : AlertDialog(context) {
    private lateinit var binding: DialogConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setText()
        setListeners()
    }

    private fun setText() {
        binding.title.text = title
        binding.description.text = description
    }

    private fun setListeners() {
        binding.positiveButton.setOnClickListener {
            dismiss()
            result(true)
        }

        binding.negativeButton.setOnClickListener {
            dismiss()
            result(false)
        }
    }
}