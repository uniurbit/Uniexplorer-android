package com.appload.einkapp.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun convertDateToHour(data: String?): String {
        if (data == null)
            return "--"
        return try {
            val originalFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = originalFormatter.parse(data)
            outFormatter.format(date)
        } catch (ex: Exception) {
            "--"
        }
    }

    fun convertStringToDate(data: String?): Date {
        if (data == null)
            return Date()
        return try {
            val originalFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            originalFormatter.parse(data)
        } catch (ex: Exception) {
            Date()
        }
    }

    fun isInTimeRange(oraInizio: String, oraFine: String): Boolean{//Controlla che l'ora sia entro 15 minuti dall'inizio e entro 15 dalla fine
        val timeWindow = 15*60*1000L //Finestra di 15 minuti
        val startingMs = convertStringToHour(oraInizio).time
        val endingMs = convertStringToHour(oraFine).time
        return System.currentTimeMillis() > (startingMs - timeWindow) && System.currentTimeMillis() < (endingMs - timeWindow)
    }
    private fun convertStringToHour(data: String?): Date {
        if (data == null)
            return Date()
        return try {
            val originalFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            originalFormatter.parse(data)
        } catch (ex: Exception) {
            Date()
        }
    }

    fun isToday(dateString: String): Boolean {
        Log.e("DATECRASH", dateString)
        val originalFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = originalFormatter.parse(dateString)
        return try {
            isToday(date)
        } catch (e: java.lang.Exception) {
            false
        }
    }

    fun isToday(date: Date?): Boolean {
        return isSameDay(date, Calendar.getInstance().time)
    }

    fun isSameDay(date1: Date?, date2: Date?): Boolean {
        require(!(date1 == null || date2 == null)) { "The dates must not be null" }
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isSameDay(cal1, cal2)
    }

    private fun isSameDay(cal1: Calendar?, cal2: Calendar?): Boolean {
        require(!(cal1 == null || cal2 == null)) { "The dates must not be null" }
        return cal1[Calendar.ERA] == cal2[Calendar.ERA] && cal1[Calendar.YEAR] == cal2[Calendar.YEAR] && cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR]
    }
}