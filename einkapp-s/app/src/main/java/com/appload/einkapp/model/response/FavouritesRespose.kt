package com.appload.einkapp.model.response

class FavouritesRespose : ArrayList<FavouritesRespose.FavouritesResposeItem>(){
    data class FavouritesResposeItem(
        val idStanza : String,
        val uuid: String,
        val majorNumber : Int,
        val minorNumber : Int,
        val nomeStanza: String,
        val nomeEdificio : String
    )
}