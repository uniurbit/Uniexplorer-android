package com.appload.einkapp.utils

import android.text.TextUtils
import android.util.Patterns
import java.math.BigInteger
import java.security.MessageDigest

object TextUtils {
    fun isValidEmail(target: String): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }
}