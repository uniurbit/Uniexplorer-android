package com.appload.einkapp.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appload.einkapp.view.base.BaseViewModel
import org.altbeacon.beacon.Beacon

class MainViewModel:BaseViewModel() {
    class Factory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel() as T
        }
    }

    val rangedBeacons: MutableLiveData<Collection<Beacon>> by lazy {
        MutableLiveData<Collection<Beacon>>()
    }

    val monitoredBeacons: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }


}