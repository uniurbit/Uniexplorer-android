package com.appload.einkapp.repository

import android.content.Context
import com.appload.einkapp.model.response.BeaconsResponse
import com.appload.einkapp.model.response.CalendarioResponse
import com.appload.einkapp.model.response.FavouritesRespose
import com.appload.einkapp.repository.network.ApiContainer
import com.appload.einkapp.repository.network.NetworkResult
import com.appload.einkapp.repository.network.ResponseCallback
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class CalendarRepository (): BaseRepository("/") {

    private var service: CalendarService = getService()

    fun getCalendar(
        idUtente:Int,
        result: (NetworkResult<ApiContainer<CalendarioResponse>>)->Unit
    ){
        service.getCalendar(idUtente).enqueue(ResponseCallback.getCallback(result))
    }
//    suspend fun getCalendar(idUtente: Int): NetworkResult<ApiContainer<CalendarioResponse>> {
//    return safeApiCall { service.getCalendar(idUtente = idUtente) }
//    }
    interface CalendarService{

//        @GET("getCalendar.php")
//        fun getCalendar(@Query("idUtente") idUtente:Int): Call<CalendarioResponse>
        @GET("account/{idUtente}/events")
        fun getCalendar(@Path("idUtente") idUtente : Int): Call<ApiContainer<CalendarioResponse>>
    }
}