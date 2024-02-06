package com.appload.einkapp.model.response

data class BeaconsResponse(
    val idEdificio: String, // S42
    val idStanza: String, // S42AB
    val majorNumber: Int, // 2
    val minorNumber: Int, // 1
    val nomeEdificio: String, // Palazzo Area 1
    val nomeStanza: String, // Aula Blu
    val testo: String, // Lezioni aggiuntive Lingua Inglese Gruppo B - Lezioni Aggiuntive Facoltative l'evento si tiene dalle 11:00 alle 13:00 Docente: Marco Neri
    val uuid: String // 4edee341-136b-4aeb-9f08-93a817c0e820
)