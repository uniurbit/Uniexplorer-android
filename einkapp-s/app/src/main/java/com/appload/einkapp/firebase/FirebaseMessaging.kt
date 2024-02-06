package com.appload.einkapp.firebase

import android.annotation.SuppressLint
import android.content.ContentValues
import android.util.Log
import com.appload.einkapp.model.response.UserResponse
import com.appload.einkapp.repository.CalendarRepository
import com.appload.einkapp.repository.network.NetworkResult
import com.appload.einkapp.utils.UserPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FirebaseMessaging : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        Log.i("FCM Data", message.toString())
        if (message.from?.contains("calendar") == true) {
            val user = getUniurbUser()
            if (user != null) {
                CalendarRepository().getCalendar(idUtente = user.id) {
                    when (it) {
                        is NetworkResult.Success -> {
                            if(it.container.data!=null){
                                UserPreferences.deleteCalendario(this)
                                UserPreferences.setCalendario(this, it.container.data)
                                Log.e(ContentValues.TAG, it.container.toString())
                            }
                        }

                        is NetworkResult.Failure -> {
                            UserPreferences.deleteCalendario(this)
                            Log.e(ContentValues.TAG, "No calendar")
                        }

                        is NetworkResult.Loading -> {

                        }
                    }
                }
            }
        }
    }


    private fun getUniurbUser(): UserResponse? {
        return if (!UserPreferences.isGuest(this)) {
            if (UserPreferences.isUniurb(this))
                UserPreferences.getUser(this)
            else null
        } else null
    }

}