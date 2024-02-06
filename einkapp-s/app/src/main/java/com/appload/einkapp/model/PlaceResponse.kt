package com.appload.einkapp.model

data class PlaceResponse(
    val UUID: Int,
    val dettagli: List<Dettagli>,
    val id: Int,
    val nome: String,
    val orarioA: String,
    val orarioDa: String
) {
    data class Dettagli(
        val riga: String
    )
    companion object{
        fun getTestObject() = PlaceResponse(
            1,
            arrayListOf(PlaceResponse.Dettagli("Test 1"),PlaceResponse.Dettagli("Test 2")),
            1,
            "Aula 1",
            "",
            ""
        )
    }
}