package com.appload.einkapp.view.common

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.appload.einkapp.databinding.DialogInfoBinding

class InfoDialog(
    context: Context,
    val title: String,
    val subtitle: String,
    val dismissed: () -> Unit
) : AlertDialog(context) {
    private lateinit var binding: DialogInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setText()
        setListeners()
        setOnDismissListener {
            dismissed()
        }
    }

    private fun setText() {
        binding.title.text = title
        binding.subtitle.text = subtitle
    }

    private fun setListeners() {
        binding.positiveButton.setOnClickListener {
            dismissed()
            dismiss()
        }
    }
}