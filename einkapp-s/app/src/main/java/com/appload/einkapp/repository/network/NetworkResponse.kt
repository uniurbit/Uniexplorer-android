package com.appload.einkapp.repository.network

import com.appload.einkapp.model.ErrorResponse
import com.google.gson.Gson
import retrofit2.Response

object NetworkResponse {

    inline fun <reified T> responseReceived(response: Response<T>?): NetworkResult<T> {
        if (response == null)
            return NetworkResult.Failure(ErrorResponse.getGeneric())

        return if (response.isSuccessful && response.body() != null) {
            NetworkResult.Success(response.body()!!)
        } else {
            NetworkResult.Failure(parseError(response.errorBody().toString()))
        }
    }

    fun parseError(error: String): ErrorResponse {
        return try {
            Gson().fromJson(error, ErrorResponse::class.java)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            ErrorResponse.getGeneric()
        }
    }

}