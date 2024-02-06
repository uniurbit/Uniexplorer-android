package com.appload.einkapp.model.request

data class LoginRequest(
    val email: String,
    val pushId: String?,
    val nome: String?,
    val cognome: String?,
    val os: String = "android"
)