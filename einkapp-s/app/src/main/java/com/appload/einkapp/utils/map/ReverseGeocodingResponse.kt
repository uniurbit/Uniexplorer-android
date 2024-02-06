package com.appload.einkapp.utils.map

data class ReverseGeocodingResponse(
    val address: Address,
    val display_name: String,
    val lat: String,
    val licence: String,
    val lon: String,
    val osm_id: String,
    val error: String?,
    val osm_type: String,
    val place_id: String
) {
    data class Address(
        val country: String,
        val country_code: String,
        val county: String,
        val hamlet: String,
        val postcode: String,
        val road: String,
        val state: String,
        val town: String,
        val village: String
    )
}