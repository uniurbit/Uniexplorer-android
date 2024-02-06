package com.appload.einkapp.view.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appload.einkapp.model.ErrorResponse
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {
    private val _error = MutableLiveData<ErrorResponse>()
    val error: MutableLiveData<ErrorResponse> get() = _error

    fun makeCall(call: suspend () -> Unit) {
        viewModelScope.launch { call() }
    }
}