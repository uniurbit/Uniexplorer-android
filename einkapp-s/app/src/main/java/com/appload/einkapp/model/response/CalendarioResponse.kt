package com.appload.einkapp.model.response

class CalendarioResponse : ArrayList<CalendarioResponseItem>()
data class CalendarioResponseItem(
    val idStanza : String,
    val uuid : String,
    val majorNumber : Int,
    val minorNumber : Int,
    val nomeStanza : String,
    val nomeEdificio : String,
    val inizio : String,
    val fine : String,
    val testo: String
)