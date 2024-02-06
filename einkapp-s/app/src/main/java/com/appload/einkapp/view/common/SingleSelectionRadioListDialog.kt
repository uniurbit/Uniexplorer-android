package com.appload.einkapp.view.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.appload.einkapp.R
import com.appload.einkapp.databinding.BottomsheetDialogSingleSelectionBinding
import com.appload.einkapp.databinding.CompRadioButtonNotOutlinedBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SingleSelectionRadioListDialog<T>(
    val title: String,
    val data: ArrayList<T>,
    val preselectedPosition: Int,
    val result: (T) -> Unit
) : BottomSheetDialogFragment() {

    private var selectedPosition: Int = 0
    private var selection: T? = null
    private lateinit var binding: BottomsheetDialogSingleSelectionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomsheetDialogSingleSelectionBinding.inflate(inflater, container, false)
        dialog?.setOnShowListener {
            val d = it as BottomSheetDialog
            val bottomSheetInternal =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheetInternal != null) {
                BottomSheetBehavior.from(bottomSheetInternal).state =
                    BottomSheetBehavior.STATE_EXPANDED
            }
        }
        setListeners()
        prepareSelection(data)
        val behavior = BottomSheetBehavior.from(binding.container)
        behavior.setPeekHeight(binding.container.height, true)
        behavior.isHideable = false
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        return binding.root
    }

    private fun setListeners() {
        binding.confirmBtn.setOnClickListener {
            if (selection != null && tag != null) {
                result(selection!!)
                dismiss()
            }
        }
    }

    private fun prepareSelection(arrayList: ArrayList<T>) {
        arrayList.forEachIndexed { index, s ->
            val id = View.generateViewId()
            val radioBtn =
                CompRadioButtonNotOutlinedBinding.inflate(layoutInflater, binding.radioGroup, true)
            radioBtn.root.id = id
            radioBtn.root.text = s.toString()
            radioBtn.root.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    selectedPosition = index
                    selection = s
                    binding.confirmBtn.isEnabled = true
                }
            }
            if (preselectedPosition == index) {
                radioBtn.root.isChecked = true
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.BaseBottomSheet
    }
}