package com.appload.einkapp.view.common

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.appload.einkapp.R
import com.appload.einkapp.databinding.DialogInfoBinding
import com.appload.einkapp.model.ErrorResponse

object ErrorDialog {

    fun showError(context: Context?, error: ErrorResponse) {
        if (context == null) return
        ErrorDialogInternal(context, error).show()
    }

    internal class ErrorDialogInternal(
        context: Context,
        val error: ErrorResponse,
    ) : AlertDialog(context) {
        private lateinit var binding: DialogInfoBinding

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = DialogInfoBinding.inflate(layoutInflater)
            setContentView(binding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setText()
            setListeners()
        }

        private fun setText() {
            binding.title.text = context.getString(R.string.attenzione)
            binding.subtitle.text = error.toString()
        }

        private fun setListeners() {
            binding.positiveButton.setOnClickListener {
                dismiss()
            }
        }
    }
}