package com.appload.einkapp.service

import android.content.Context
import android.util.Log
import com.appload.einkapp.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


object ServiceFactory {

    inline fun <reified T> createServiceTokenUnchecked(suffix: String?, context: Context): T {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient().newBuilder()
        httpClient
            .addInterceptor(interceptor)
        httpClient.retryOnConnectionFailure(true)

        val gson = GsonBuilder()
            .setLenient()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(BuildConfig.BASE_URL + suffix)
            .client(httpClient.build())
            .build()
        Log.e("SERVICE", "created ${T::class.simpleName}")
        return retrofit.create(T::class.java)
    }

    inline fun <reified T> createService(suffix: String?, context: Context): T {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient().newBuilder()
        httpClient
            .addInterceptor(com.appload.einkapp.api.TokenInterceptor())
            .addInterceptor(com.appload.einkapp.api.ContentTypeInterceptor())
            .addInterceptor(interceptor)
        httpClient.retryOnConnectionFailure(true)

        val gson = GsonBuilder()
            .setLenient()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create()

        val retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(BuildConfig.BASE_URL + suffix)
            .client(httpClient.build())
            .build()
        Log.e("SERVICE", "created ${T::class.simpleName}")
        return retrofit.create(T::class.java)
    }

    inline fun <reified T> createUnauthorizedService(suffix: String?): T {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient().newBuilder()
        httpClient
            .addInterceptor(interceptor)
            .addInterceptor(com.appload.einkapp.api.ContentTypeInterceptor())
        httpClient.retryOnConnectionFailure(true)

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(BuildConfig.BASE_URL + suffix)
            .client(httpClient.build())
            .build()
        Log.e("UNAUTHORIZED SERVICE", "created ${T::class.simpleName}")
        return retrofit.create(T::class.java)
    }
}