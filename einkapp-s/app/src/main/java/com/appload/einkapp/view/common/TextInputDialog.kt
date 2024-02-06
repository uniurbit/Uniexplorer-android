package com.appload.einkapp.view.common

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputFilter
import android.view.LayoutInflater
import androidx.core.widget.addTextChangedListener
import com.appload.einkapp.databinding.DialogTextInputBinding
import com.appload.einkapp.databinding.DialogTextMultilineInputBinding

object TextInputDialog {
    fun showDialog(
        context: Context,
        title: String,
        hint: String,
        positiveButtonText: String,
        result: (String) -> Unit
    ) {
        val binding = DialogTextInputBinding.inflate(LayoutInflater.from(context))
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        binding.title.text = title
        binding.input.hint = hint
        binding.positiveButton.text = positiveButtonText

        builder.setView(binding.root)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        binding.input.requestFocus()

        binding.positiveButton.setOnClickListener {
            result(binding.input.text.toString().trim())
            dialog.dismiss()
        }

        binding.input.addTextChangedListener {
            binding.positiveButton.isEnabled = it.isNullOrBlank().not()
        }
    }

    fun showDialogMultiline(
        context: Context,
        title: String,
        hint: String,
        positiveButtonText: String,
        maxCharacters: Int,
        result: (String) -> Unit
    ) {
        val binding = DialogTextMultilineInputBinding.inflate(LayoutInflater.from(context))
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        binding.title.text = title
        binding.input.hint = hint
        binding.positiveButton.text = positiveButtonText
        binding.input.filters = arrayOf(InputFilter.LengthFilter(maxCharacters))
        builder.setView(binding.root)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        binding.input.requestFocus()

        binding.positiveButton.setOnClickListener {
            result(binding.input.text.toString().trim())
            dialog.dismiss()
        }

        binding.input.addTextChangedListener {
            binding.positiveButton.isEnabled = it.isNullOrBlank().not()
        }
    }

}