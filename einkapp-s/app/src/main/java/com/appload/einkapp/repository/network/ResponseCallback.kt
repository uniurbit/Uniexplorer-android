package com.appload.einkapp.repository.network

import com.appload.einkapp.model.ErrorResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ResponseCallback {
    inline fun <reified T> getCallback(crossinline result: (NetworkResult<T>) -> Unit): Callback<T> {
        val callback = object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (!response.isSuccessful) {
                    val error = NetworkResponse.parseError(response.errorBody().toString())
                    if (error != null) {
                        result(NetworkResult.Failure(error))
                    } else {
                        if (response.code() == 500) {
                            result(NetworkResult.Failure(ErrorResponse.serverError()))
                        } else {
                            result(NetworkResult.Failure(ErrorResponse.getGeneric()))
                        }
                    }
                } else {
                    result(NetworkResult.Success(response.body()!!))
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                result(NetworkResult.Failure(ErrorResponse.getGeneric()))
            }

        }
        return callback
    }
}