package com.appload.einkapp.view.common

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import com.appload.einkapp.databinding.DialogSingleSelectionBinding


class SingleSelectionDialog<T>(
    context: Context,
    val title: String,
    val data: Array<T>,
    val result: (T) -> Unit
) :
    AlertDialog(context) {
    private lateinit var binding: DialogSingleSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogSingleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prepareStage()
        setCancelable(true)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun prepareStage() {
        binding.title.text = title

        val arrayAdapter =
            ArrayAdapter<String>(context, android.R.layout.select_dialog_singlechoice)
        data.forEach { arrayAdapter.add(it.toString()) }

        binding.list.adapter = arrayAdapter

        binding.list.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = data[position]
            result(selectedItem)
            this.dismiss()
        }

        binding.close.setOnClickListener { this.dismiss() }
    }
}