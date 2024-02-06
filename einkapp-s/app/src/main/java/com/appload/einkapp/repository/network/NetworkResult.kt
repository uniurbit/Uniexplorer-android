package com.appload.einkapp.repository.network

import com.appload.einkapp.model.ErrorResponse

sealed class NetworkResult<out T> {
    class Success<out T>(val container: T) : NetworkResult<T>()
    class Failure<out T>(val exception: ErrorResponse) : NetworkResult<T>()
    class Loading<out T> : NetworkResult<T>()
}
