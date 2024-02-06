package com.appload.einkapp.api

import okhttp3.Interceptor
import okhttp3.Response

class ContentTypeInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        request.addHeader("Content-Type", "application/json")
        request.addHeader("API-Key", "1f63ab5249edaa252836280ed2cf5e6d")
        request.addHeader("Accept", "application/json")
        return chain.proceed(request.build())
    }
}