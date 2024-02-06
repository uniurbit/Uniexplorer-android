package com.appload.einkapp

import android.app.*
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.Observer
import com.appload.einkapp.utils.UserPreferences
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.messaging.FirebaseMessaging
import com.onesignal.OneSignal
import org.altbeacon.beacon.Beacon

class EinkApp : Application() {
    override fun onCreate() {
        super.onCreate()
        //setupForegroundService()
        //setOneSignal()
        setFirebase()
    }

    private fun setFirebase() {
        FirebaseApp.initializeApp(this)

        Log.d("FirebaseProps", Firebase.app.options.projectId.toString())
        Log.d("FirebaseProps", Firebase.app.options.applicationId)
        Log.d("FirebaseProps", Firebase.app.name)
        FirebaseMessaging.getInstance().subscribeToTopic("calendar")
            .addOnCompleteListener { task ->
                var msg = "Subscribed to topic CALENDAR"
                if (!task.isSuccessful) {
                    msg = "Subscribe to topic CALENDAR failed"
                }
                Log.d(TAG, msg)
            }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            UserPreferences.setPushId(this,token)
            Log.d("FirebaseProps", "Firebase pushId: $token")

        })
    }


}