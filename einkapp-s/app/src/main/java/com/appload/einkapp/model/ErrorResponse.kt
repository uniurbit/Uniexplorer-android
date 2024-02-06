package com.appload.einkapp.model

data class ErrorResponse(
    val errorCode: String,
    val error: String
) {
    override fun toString(): String {
        return error
    }

    companion object {
        fun getGeneric(): ErrorResponse {
            return ErrorResponse("KO", "Generic error")
        }

        fun serverError(): ErrorResponse {
            return ErrorResponse("KO", "Server error, please retry again later")
        }
    }
}
