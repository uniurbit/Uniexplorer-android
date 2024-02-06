package com.appload.einkapp.utils

const val IBEACON_LAYOUT = "m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"
const val TIME_TO_LIVE = 15000L //ttl for every beacon
const val TIME_BEFORE_NOTIFICATION =
    5000L //Dopo questo tempo vicino al beacon viene mostrata la notifica
const val DISTANCE_THRESHOLD = 5.0 //metri entro i quali ricevere una notifica
const val ACTION_STOP_FOREGROUND = "ACTION_STOP_FOREGROUND"
const val NOTIFICATION_CLICKED = "NOTIFICATION_CLICKED"
const val SEC_TIC = 1

val RANGE_HIGHEST = 0f..2.5f
val RANGE_HIGH = 2.5f..6f
val RANGE_MID = 6f..8f
val RANGE_LOW = 0f..2f
