package com.appload.einkapp.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.appload.einkapp.utils.UserPreferences
import com.appload.einkapp.utils.Utils

class SnoozeBeaconReceiver : BroadcastReceiver() {//Silenzia le notifiche del beacon per un'ora

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == SNOOZE_BEACON){
            val uuid = intent.getStringExtra(BEACON_UUID)
            val major = intent.getStringExtra(BEACON_MAJOR)
            val minor = intent.getStringExtra(BEACON_MINOR)
            if(uuid != null && major != null && minor != null){
                snoozeBeacon(uuid, major, minor, context)
            }
        }
    }

    private fun snoozeBeacon(uuid: String, major: String, minor: String, context: Context) {
        UserPreferences.snoozeBeacon(context, uuid, major, minor)
        NotificationManagerCompat.from(context).cancel(Utils.notificationIdFromUuids(uuid, major, minor))//Rimuovo notifica
    }

    companion object{
        const val SNOOZE_BEACON = "SNOOZE_BEACON"
        const val BEACON_UUID = "BEACON_UUID"
        const val BEACON_MAJOR = "BEACON_MAJOR"
        const val BEACON_MINOR = "BEACON_MINOR"
    }
}