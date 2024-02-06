package com.appload.einkapp.utils

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.inputmethod.InputMethodManager
import com.appload.einkapp.model.response.FavouritesRespose
import org.altbeacon.beacon.Beacon
import kotlin.math.min

object Utils {

    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    fun showKeyboard(context: Context) {
        managementKeyboard(context, InputMethodManager.SHOW_FORCED)
    }

    fun closeKeyboard(context: Context) {
        managementKeyboard(context, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private fun managementKeyboard(context: Context, hideImplicitOnly: Int) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(hideImplicitOnly, 0)
    }

    fun createDialog(context: Context, title: String, text: String) {
        AlertDialog.Builder(context).create().apply {
            setTitle(title)
            setMessage(text)
            setButton(
                AlertDialog.BUTTON_NEGATIVE, "OK"
            ) { dialog, _ -> dialog.dismiss() }
            show()
        }
    }

    fun isSameBeacon(
        beacon: Beacon,
        uuidB: String,
        major: String,
        minor: String
    ): Boolean {
        return (beacon.id1.toString().equals(uuidB, true) && beacon.id2.toString()
            .equals(major, true) && beacon.id3.toString().equals(minor, true))
    }
    fun isSameBeaconFavorite(
        beacon: FavouritesRespose.FavouritesResposeItem,
        uuidB: String,
        major: String,
        minor: String
    ): Boolean {
        return (beacon.uuid.toString().equals(uuidB, true) && beacon.majorNumber.toString()
            .equals(major, true) && beacon.minorNumber.toString().equals(minor, true))
    }

    fun isSameBeacon(
        uuidA: String,
        majorA: String,
        minorA: String,
        uuidB: String,
        majorB: String,
        minorB: String
    ): Boolean {
        return (uuidA.equals(uuidB, true) && majorA
            .equals(majorB, true) && minorA.equals(minorB, true))
    }

    fun notificationIdFromUuids(
        uuidB: String,
        major: String,
        minor: String
    ): Int{
        return (uuidB + major + minor).hashCode()
    }

    fun notificationIdFromBeacon(
        beacon: Beacon
    ): Int{
        return notificationIdFromUuids(
            uuidB = beacon.id1.toString(),
            major = beacon.id2.toString(),
            minor = beacon.id3.toString()
        )
    }

}