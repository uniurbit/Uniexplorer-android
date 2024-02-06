package com.appload.einkapp.repository

import android.content.Context
import android.util.Log
import com.appload.einkapp.BuildConfig
import com.appload.einkapp.api.ContentTypeInterceptor
import com.appload.einkapp.model.ErrorResponse
import com.appload.einkapp.repository.network.ApiContainer
import com.appload.einkapp.repository.network.NetworkResult
import com.google.gson.GsonBuilder
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException

abstract class BaseRepository(val suffix: String) {
    inline fun <reified T> getService(): T {
//        if (token == null) return null
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient().newBuilder()
        httpClient
            .addInterceptor(com.appload.einkapp.api.TokenInterceptor())
            .addInterceptor(ContentTypeInterceptor())
//            .addInterceptor(TokenInaTypeInterceptor())
//            .addInterceptor(UserAgentInterceptor())
            .addInterceptor(interceptor)
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
        Log.e("SERVICE", "created ${T::class.simpleName}")
        return retrofit.create(T::class.java)
    }

    // we'll use this function in all
    // repos to handle api errors.
    suspend fun <T> safeApiCall(apiToBeCalled: suspend () -> Response<T>): NetworkResult<T> {

        // Returning api response
        // wrapped in Resource class
        return withContext(Dispatchers.IO) {
            try {

                // Here we are calling api lambda
                // function that will return response
                // wrapped in Retrofit's Response class
                val response: Response<T> = apiToBeCalled()
                if (response.isSuccessful) {
                    // In case of success response we
                    // are returning Resource.Success object
                    // by passing our data in it.
                    val data = response.body()!! as ApiContainer<*>
                    if (data.status.equals("success", true)) {
                        NetworkResult.Success(container = response.body()!!)
                    } else {
                        NetworkResult.Failure(ErrorResponse("KO", data.message))
                    }
                } else {
                        // parsing api's own custom json error
                        // response in ExampleErrorResponse pojo
                        val errorMessage: String? = convertErrorBody(response.errorBody())
                        val error = ErrorResponse(
                            response.code().toString(),
                            errorMessage ?: response.errorBody().toString()
                        )
                        // Simply returning api's own failure message
                        NetworkResult.Failure(error)
                }

            } catch (e: HttpException) {
                // Returning HttpException's message
                // wrapped in Resource.Error
                NetworkResult.Failure(ErrorResponse(e.code().toString(), e.message()))
            } catch (e: IOException) {
                // Returning no internet message
                // wrapped in Resource.Error
                NetworkResult.Failure(ErrorResponse("100", "No internet connection"))
            } catch (e: Exception) {
                // Returning 'Something went wrong' in case
                // of unknown error wrapped in Resource.Error
                e.printStackTrace()
                NetworkResult.Failure(ErrorResponse("400", "Generic error"))
            }
        }
    }

    // If you don't wanna handle api's own
    // custom error response then ignore this function
    private fun convertErrorBody(errorBody: ResponseBody?): String? {
        return try {
            errorBody?.source()?.let {
                val moshiAdapter = Moshi.Builder().build().adapter(String::class.java)
                moshiAdapter.fromJson(it)
            }
        } catch (exception: Exception) {
            null
        }
    }
}
