package com.appload.einkapp.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.appload.einkapp.repository.ApiRepository
import com.appload.einkapp.view.ApiViewModel

abstract class BaseFragment : Fragment() {

    val viewModelApi: ApiViewModel by activityViewModels {
        ApiViewModel.Factory(
            ApiRepository()
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = getFragmentView(inflater, container)
        prepareStage()
        setListeners()
        return view
    }

    abstract fun setListeners()

    abstract fun prepareStage()

    abstract fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View


//    fun closeKeyBoard() =
//        Utils.closeKeyboard(requireContext())
//
//    fun openKeyBoard() =
//        Utils.showKeyboard(requireContext())

}