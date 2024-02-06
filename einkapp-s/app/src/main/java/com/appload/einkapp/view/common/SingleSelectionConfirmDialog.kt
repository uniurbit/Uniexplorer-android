package com.appload.einkapp.view.common

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import com.appload.einkapp.databinding.DialogSingleSelectionConfirmBinding

class SingleSelectionConfirmDialog<T>(
    context: Context,
    val title: String,
    val data: Array<T>,
    val result: (T) -> Unit
) :
    AlertDialog(context) {

    private lateinit var binding: DialogSingleSelectionConfirmBinding
    private var selectedItem: T? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogSingleSelectionConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prepareStage()
        setListeners()
        setCancelable(true)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setListeners() {
        binding.list.setOnItemClickListener { _, view, position, _ ->
            selectedItem = data[position]
            binding.confirmButton.isEnabled = true
            binding.selectedValue.text = selectedItem.toString()
        }
    }

    private fun prepareStage() {
        binding.title.text = title

        val arrayAdapter =
            ArrayAdapter<String>(context, android.R.layout.select_dialog_item)
        data.forEach { arrayAdapter.add(it.toString()) }

        binding.list.adapter = arrayAdapter
        binding.close.setOnClickListener { this.dismiss() }

        binding.confirmButton.setOnClickListener {
            result(selectedItem!!)
            this.dismiss()
        }
    }
}