package com.appload.einkapp.model.response

class LuoghiResponse : ArrayList<LuoghiResponse.LuoghiResponseItem>() {

    data class LuoghiResponseItem(
        val idEdificio: String,
        val nomeEdificio: String,
        val luoghi: List<Luogo>
    ) {
        data class Luogo(
            val idStanza: String,
            val uuid: String,
            val majorNumber: Int,
            val minorNumber: Int,
            val nomeStanza: String,
            val testo: String
        ) {

            override fun toString(): String {
                return "Luogo(uuid='$uuid', majorNumber=$majorNumber, minorNumber=$minorNumber, nomeStanza='$nomeStanza')"
            }

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Luogo

                if (uuid != other.uuid) return false
                if (majorNumber != other.majorNumber) return false
                if (minorNumber != other.minorNumber) return false

                return true
            }

            override fun hashCode(): Int {
                var result = uuid.hashCode()
                result = 31 * result + majorNumber
                result = 31 * result + minorNumber
                return result
            }


        }
    }
}