package com.appload.einkapp.model

class ArrayTypedResponse : ArrayList<com.appload.einkapp.model.ArrayTypedResponse.MyObject>() {
    data class MyObject(
        val param: Any?
    )
}