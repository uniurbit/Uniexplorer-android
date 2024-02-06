package com.appload.einkapp.utils.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.appload.einkapp.R
import com.appload.einkapp.repository.network.NetworkResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object LocationHelper {
    enum class AccessType() {
        FINE, COARSE
    }

    sealed class DLPS() {
        data class Disabled(val message: String) : DLPS()
        data class Refused(val message: String) : DLPS()
        data class Granted(val accessType: AccessType) : DLPS()
    }

    private fun hasFineAccess(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasCoarseAccess(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun canGetLocation(activity: Activity?): DLPS {
        val locationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return DLPS.Disabled(activity?.getString(R.string.gps_disabled) ?: "GPS Error")

        return if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (hasFineAccess(activity)) {
                DLPS.Granted(AccessType.FINE)
            } else if (hasCoarseAccess(activity)) {
                DLPS.Granted(AccessType.COARSE)
            } else {
                DLPS.Refused(activity.getString(R.string.gps_permission_refused))
            }
        } else {
            DLPS.Disabled(activity.getString(R.string.gps_disabled))
        }
    }

    private fun getDefaultLocation(): LatLng {
        return LatLng(45.4642, 9.1900)
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(
        fusedLocationProviderClient: FusedLocationProviderClient,
        location: (DeviceLocation) -> Unit
    ) {
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener {
                if (it != null)
                    location(DeviceLocation.Location(LatLng(it.latitude, it.longitude)))
                else
                    location(DeviceLocation.Error(getDefaultLocation()))
            }
            .addOnCanceledListener {
                location(DeviceLocation.Error(getDefaultLocation()))
            }
            .addOnFailureListener {
                location(DeviceLocation.Error(getDefaultLocation()))
            }
    }

    fun getAddressFromCoordinates(
        coordinates: LatLng,
        result: (NetworkResult<ReverseGeocodingResponse>) -> Unit
    ) {
//        val service = createService()
//        service.getAddress(coordinates.latitude, coordinates.longitude)
//            .enqueue(ResponseCallback.getCallback(result))
    }

    private fun createService(): ReverseGeocodingService {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient().newBuilder()
        httpClient
            .addInterceptor(interceptor)
        httpClient.retryOnConnectionFailure(true)

        val retrofit = Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl("https://nominatim.openstreetmap.org/")
            .client(httpClient.build())
            .build()
        return retrofit.create(ReverseGeocodingService::class.java)
    }

    private interface ReverseGeocodingService {
        @GET("reverse")
        fun getAddress(
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("format") format: String = "json"
        ): Call<String>
    }
}