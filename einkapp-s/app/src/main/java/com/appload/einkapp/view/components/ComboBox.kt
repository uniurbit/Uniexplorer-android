package com.appload.einkapp.view.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import com.appload.einkapp.R
import com.appload.einkapp.databinding.CompComboBoxBinding
import com.appload.einkapp.view.common.SingleSelectionDialog

class ComboBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: CompComboBoxBinding =
        CompComboBoxBinding.inflate(LayoutInflater.from(context), this, true)

    private var title = ""
    private var isActive = false

    init {
        context.withStyledAttributes(attrs, R.styleable.ComboBox) {
            title = getString(R.styleable.ComboBox_hint) ?: "Please, select your item"
            binding.selectedCategory.hint = title
        }
    }

    fun required() {
        binding.container.isSelected = true
    }

    fun disable() {
        isActive = false
        binding.selectedCategory.text = null
        binding.container.background =
            resources.getDrawable(R.drawable.background_input_field_4dp_disabled, null)
    }

    fun setValue(value: String) {
        binding.container.isSelected = false
        binding.selectedCategory.text = value
    }

    fun <T> setListener(data: Array<T>, result: (T) -> Unit) {
        isActive = true
        binding.container.setBackgroundResource(R.drawable.background_input_field_4dp)
        binding.container.setOnClickListener {
            if (isActive)
                SingleSelectionDialog(context, title, data) {
                    setValue(it.toString())
                    result(it)
                }.show()
        }
    }

}