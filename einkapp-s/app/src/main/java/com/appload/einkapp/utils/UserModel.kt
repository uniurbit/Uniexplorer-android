package com.appload.einkapp.utils

open class UserModel(
    val args :User
) {
    data class User(
        val username:String
    )
}