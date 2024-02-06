package com.appload.einkapp.model.response

import android.net.Uri
import android.provider.ContactsContract.DisplayPhoto
import com.appload.einkapp.model.PlaceResponse

data class UserResponse(
    val cognome: String?,
    val corsoStudi: String?,
    val email: String?,
    val id: Int,
    val nome: String?,
    val poi: List<Poi>?
) {

    data class Poi(
        val UUID: Int?,
        val dettagli: List<PlaceResponse.Dettagli>?,
        val id: Int?,
        val nome: String?,
        val oraFine: String?,
        val oraInizio: String?
    )
}