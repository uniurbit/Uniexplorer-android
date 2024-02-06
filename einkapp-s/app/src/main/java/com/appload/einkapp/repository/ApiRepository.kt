package com.appload.einkapp.repository

import android.content.Context
import com.appload.einkapp.model.request.DeleteLuogoRequest
import com.appload.einkapp.model.request.LoginRequest
import com.appload.einkapp.model.request.LogoutRequest
import com.appload.einkapp.model.request.LuogoRequest
import com.appload.einkapp.model.response.*
import com.appload.einkapp.repository.network.ApiContainer
import com.appload.einkapp.repository.network.NetworkResult
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

class ApiRepository () : BaseRepository("/") {

    private var service: ApiService

    init {
        service = getService<ApiService>()
    }

    suspend fun getLuogo(uuid: String,major:String,minor: String): NetworkResult<ApiContainer<BeaconsResponse>> {
        return safeApiCall { service.getLuoghi(uuid,major,minor) }
        // return NetworkResponse.responseReceived(getService<ApiService>()?.getLuoghi(id))
    }

    suspend fun getFavourites(idUtente: Int): NetworkResult<ApiContainer<FavouritesRespose>> {
        return safeApiCall { service.getFavourites(idUtente) }
        //return NetworkResponse.responseReceived(getService<ApiService>()?.getFavourites(idUtente))
    }

    suspend fun getCalendar(idUtente: Int): NetworkResult<ApiContainer<CalendarioResponse>> {
        return safeApiCall { service.getCalendar(idUtente) }
        //return NetworkResponse.responseReceived(getService<ApiService>()?.getCalendar(idUtente))
    }

    suspend fun addLuogo(idUtente: Int,luogo: LuogoRequest): NetworkResult<ApiContainer<String>> {
        return safeApiCall { service.addLuogo(idUtente,luogo) }

        //return NetworkResponse.responseReceived(getService<ApiService>()?.addLuogo(luogo))
    }

    suspend fun login(user: LoginRequest): NetworkResult<ApiContainer<UserResponse>> {
        return safeApiCall { service.login(user) }

        //return NetworkResponse.responseReceived(getService<ApiService>()?.login(user))
    }

    suspend fun logout(user: LogoutRequest): NetworkResult<ApiContainer<String>> {
        return safeApiCall { service.logout(user) }

        //return NetworkResponse.responseReceived(getService<ApiService>()?.login(user))
    }

    suspend fun deleteLuogo(idUtente : String,luogo: DeleteLuogoRequest): NetworkResult<ApiContainer<String>> {
        return safeApiCall { service.deleteLuogo(idUtente,luogo) }

        //return NetworkResponse.responseReceived(getService<ApiService>()?.deleteLuogo(luogo))
    }

    suspend fun getAllLuoghi(): NetworkResult<ApiContainer<LuoghiResponse>> {
        return safeApiCall { service.getAllLuoghi() }

        //return NetworkResponse.responseReceived(getService<ApiService>().getAllLuoghi())
    }


    interface ApiService {

        @GET("account/{idUtente}/bookmarks")
        suspend fun getFavourites(@Path("idUtente") idUtente: Int): Response<ApiContainer<FavouritesRespose>>

//        @GET("getCalendar.php")
//        suspend fun getCalendar(@Query("idUtente") idUtente: Int): Response<ApiContainer<CalendarioResponse>>
        @GET("account/{idUtente}/events")
        suspend fun getCalendar(@Path("idUtente") idUtente : Int): Response<ApiContainer<CalendarioResponse>>

        @POST("account/{idUtente}/bookmarks")
        suspend fun addLuogo(@Path("idUtente") idUtente : Int, @Body request: LuogoRequest): Response<ApiContainer<String>>

        @POST("accounts/login")
        suspend fun login(@Body request: LoginRequest): Response<ApiContainer<UserResponse>>

        @POST("accounts/logout")
        suspend fun logout(@Body request: LogoutRequest): Response<ApiContainer<String>>
        @HTTP(method = "DELETE", path = "account/{idUtente}/bookmarks", hasBody = true)
        suspend fun deleteLuogo(@Path("idUtente") idUtente : String , @Body request: DeleteLuogoRequest): Response<ApiContainer<String>>

        @GET("beacons")
        suspend fun getAllLuoghi(): Response<ApiContainer<LuoghiResponse>>
        @GET("beacons/{uuid}/{major}/{minor}")
        suspend fun getLuoghi(@Path("uuid") uuid: String,@Path("major") major: String,@Path("minor") minor: String): Response<ApiContainer<BeaconsResponse>>



    }
}