package com.appload.einkapp.utils

import android.content.Context
import com.appload.einkapp.model.Luoghi
import com.appload.einkapp.model.internal.SnoozedBeacons
import com.appload.einkapp.model.request.DeleteLuogoRequest
import com.appload.einkapp.model.response.CalendarioResponse
import com.appload.einkapp.model.response.FavouritesRespose
import com.appload.einkapp.model.response.LuoghiResponse
import com.appload.einkapp.model.response.UserResponse
import com.google.gson.Gson
import org.altbeacon.beacon.Beacon


object UserPreferences : MyPreferences("USER_PREFS") {

    private const val USERINFO = "USERINFO"
    private const val USER_PHOTO = "USER_PHOTO"
    private const val GUEST = "GUEST"
    private const val NOTIFICATIONS = "NOTIFICATIONS"
    private const val ALL_LUOGHI = "ALL_LUOGHI"
    private const val FOREGROUND_ENABLED = "FOREGROUND_ENABLED"
    private const val CALENDAR = "CALENDAR"
    private const val FAVOURITES = "FAVOURITES"
    private const val SNOOZED_BEACONS = "SNOOZED_BEACONS"
    private const val SNOOZE_TIMER = 3600000L //timer silenzia beacon per un'ora

    const val CALENDARIO = "CALENDARIO"
    const val PREFERITI = "PREFERITI"
    const val TUTTI = "TUTTI"
    const val PUSH_ID = "PUSH_ID"

    fun setUser(context: Context, user: UserResponse) {
        getPrefs(context)?.edit()?.putString(USERINFO, Gson().toJson(user))?.apply()
        if (user.email != null && user.email.contains("uniurb"))//Abilito di default le notifiche per i punti di interesse
            setNotifications(context, mutableSetOf(CALENDARIO, PREFERITI, TUTTI))
        else
            setNotifications(context, mutableSetOf(PREFERITI))
        getPrefs(context)?.edit()?.putBoolean(GUEST, false)?.apply()
    }

    fun setUserPhoto(context: Context, userPhoto: String) {
        getPrefs(context)?.edit()?.putString(USER_PHOTO, userPhoto)?.apply()
    }

    fun getUserPhoto(context: Context): String {
        return getPrefs(context)?.getString(USER_PHOTO, null).toString()
    }

    fun getUser(context: Context): UserResponse? {
        return try {
            val userRaw = getPrefs(context)?.getString(USERINFO, null)
            Gson().fromJson(userRaw, UserResponse::class.java)
        } catch (e: Exception) {
            return null
        }
    }

    fun logout(context: Context) {
        try {
            getPrefs(context)?.edit()?.remove(USERINFO)?.apply()
            getPrefs(context)?.edit()?.remove(CALENDAR)?.apply()
            getPrefs(context)?.edit()?.remove(FAVOURITES)?.apply()
            getPrefs(context)?.edit()?.remove(USER_PHOTO)?.apply()
            cleanNotifications(context)
            setGuest(context, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setGuest(context: Context, isGuest: Boolean) {
        getPrefs(context)?.edit()?.putBoolean(GUEST, isGuest)?.apply()
        setNotifications(context, mutableSetOf(TUTTI))
    }

    fun setPushId(context: Context, pushId: String) {
        getPrefs(context)?.edit()?.putString(PUSH_ID, pushId)?.apply()
    }

    fun getPushId(context: Context): String? {
        return getPrefs(context)?.getString(PUSH_ID, null)
    }

    fun isGuest(context: Context): Boolean {
        return getPrefs(context)?.getBoolean(GUEST, false) ?: false
    }

    fun isUniurb(context: Context): Boolean {
        return getUser(context)?.email?.contains("uniurb") ?: false
    }

    fun setNotifications(context: Context, list: MutableSet<String>) {
        getPrefs(context)?.edit()?.putStringSet(NOTIFICATIONS, list)?.apply()
    }

    fun cleanNotifications(context: Context) {
        this.getPrefs(context)?.edit()?.remove(NOTIFICATIONS)?.apply()
    }

    fun getNotifications(context: Context): MutableSet<String>? {
        return getPrefs(context)?.getStringSet(NOTIFICATIONS, mutableSetOf())
    }

    fun setAllLuoghi(context: Context, luoghi: ArrayList<LuoghiResponse.LuoghiResponseItem.Luogo>) {
        getPrefs(context)?.edit()?.putString(ALL_LUOGHI, Gson().toJson(luoghi))?.apply()
    }

    fun getAllLuoghi(context: Context): Luoghi? {
        val string = luoghi(context)
        return try {
            val data = Gson().fromJson(string, Luoghi::class.java)
            data.addAll(testItems()) //todo: remove test item
            data
        } catch (e: Exception) {
            val data = Luoghi()
            data.addAll(testItems()) //todo: remove test item
            data
        }
    }

    //todo: remove test item
    fun testItems(): ArrayList<LuoghiResponse.LuoghiResponseItem.Luogo> {
        return arrayListOf(LuoghiResponse.LuoghiResponseItem.Luogo(
            "idStanza",
            "b69d5efa-40c7-4559-9508-d7a63a53f0cc",
            4,
            1,
            "Test Appload 1",
            "Testo normale"
        ),
            LuoghiResponse.LuoghiResponseItem.Luogo(
                "idStanza",
                "b69d5efa-40c7-4559-9508-d7a63a53f0cc",
                5,
                2,
                "Test Appload 2",
                "Testo normale"
            )
        )

    }

    private fun luoghi(context: Context): String? {
        return getPrefs(context)?.getString(ALL_LUOGHI, null)
    }

    fun setForegroundEnabled(context: Context, enabled: Boolean) {
        getPrefs(context)?.edit()?.putBoolean(FOREGROUND_ENABLED, enabled)?.apply()
    }

    fun isForegroundEnabled(context: Context): Boolean {
        return getPrefs(context)?.getBoolean(FOREGROUND_ENABLED, true) ?: false
    }

    fun setCalendario(context: Context, calendario: CalendarioResponse) {
        getPrefs(context)?.edit()?.putString(CALENDAR, Gson().toJson(calendario))?.apply()
    }

    fun getCalendario(context: Context): CalendarioResponse? {
        return try {
            val calendarRaw = getPrefs(context)?.getString(CALENDAR, null)
            Gson().fromJson(calendarRaw, CalendarioResponse::class.java)
        } catch (e: Exception) {
            return null
        }
    }

    fun deleteCalendario(context: Context) {
        getPrefs(context)?.edit()?.remove(CALENDAR)?.apply()
    }

    fun setFavourites(context: Context, favourites: FavouritesRespose) {
        getPrefs(context)?.edit()?.putString(FAVOURITES, Gson().toJson(favourites))?.apply()
    }

    fun getFavourites(context: Context): FavouritesRespose? {
        return try {
            val favouritesRaw = getPrefs(context)?.getString(FAVOURITES, null)
            Gson().fromJson(favouritesRaw, FavouritesRespose::class.java)
        } catch (e: Exception) {
            return null
        }
    }

    fun deleteFavourite(context: Context, deleteRequest: DeleteLuogoRequest) {
        val favourites = getFavourites(context)
        favourites?.removeIf { it.uuid == deleteRequest.uuid && it.majorNumber == deleteRequest.major && it.minorNumber == deleteRequest.minor }
        if (favourites != null) setFavourites(context, favourites)
    }

    fun snoozeBeacon(context: Context, uuid: String, major: String, minor: String) {
        val snoozedRaw = getPrefs(context)?.getString(SNOOZED_BEACONS, null)
        try {
            var snoozedBeacons = Gson().fromJson(snoozedRaw, SnoozedBeacons::class.java)
            if (snoozedBeacons == null) {
                snoozedBeacons = SnoozedBeacons(arrayListOf())
            }
            val alreadySnoozed = snoozedBeacons.beacons.find {
                Utils.isSameBeacon(uuid, major, minor, it.uuid, it.major, it.minor)
            }
            if (alreadySnoozed != null) {
                alreadySnoozed.lastTimeSnoozed = System.currentTimeMillis()
            } else
                snoozedBeacons.beacons.add(
                    SnoozedBeacons.BeaconInfo(
                        uuid = uuid,
                        major = major,
                        minor = minor,
                        lastTimeSnoozed = System.currentTimeMillis()
                    )
                )
            updateSnoozedBeacons(context, snoozedBeacons)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateSnoozedBeacons(context: Context, snoozedBeacons: SnoozedBeacons) {
        getPrefs(context)?.edit()?.putString(SNOOZED_BEACONS, Gson().toJson(snoozedBeacons))
            ?.apply()
    }

    fun isBeaconSnoozed(context: Context, beacon: Beacon): Boolean {
        val snoozedRaw = getPrefs(context)?.getString(SNOOZED_BEACONS, null)
        try {
            var snoozedBeacons = Gson().fromJson(snoozedRaw, SnoozedBeacons::class.java)
            if (snoozedBeacons == null) {
                snoozedBeacons = SnoozedBeacons(arrayListOf())
            }
            val alreadySnoozed = snoozedBeacons.beacons.find {
                Utils.isSameBeacon(
                    beacon.id1.toString(),
                    beacon.id2.toString(),
                    beacon.id3.toString(),
                    it.uuid,
                    it.major,
                    it.minor
                )
            }
            return if (alreadySnoozed != null) {
                if (System.currentTimeMillis() - SNOOZE_TIMER > alreadySnoozed.lastTimeSnoozed) {//Tempo scaduto
                    snoozedBeacons.beacons.remove(alreadySnoozed)
                    updateSnoozedBeacons(context, snoozedBeacons)
                    false
                } else true //Ancora silenziato
            } else false //Non è mai stato silenziato
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}