package com.appload.einkapp.api

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
//        val user = MyPreferences.getUser(context)
//        if (user != null) {
//            val request = chain.request().newBuilder()
//            request.addHeader("Authorization", "Bearer ${user.token}")
//            return chain.proceed(request.build())
//        }
        val token = ""
        val request = chain.request().newBuilder()
        request.addHeader("Authorization", "Bearer $token")
        Log.e("TokenIncerceptor", "Bearer: $token")
        return chain.proceed(request.build())
    }
}
