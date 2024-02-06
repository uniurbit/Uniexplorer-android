package com.appload.einkapp.repository.network

data class ApiContainer<T>(
    val status: String,
    val message: String,
    val data: T?
)
