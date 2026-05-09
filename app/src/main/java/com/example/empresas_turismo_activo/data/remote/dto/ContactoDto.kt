package com.example.empresas_turismo_activo.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ContactoDto(
    @SerializedName("concejo") val concejo: String?,
    /** Zona territorial descriptiva (API); se concatena dentro de dirección persistida como texto. */
    @SerializedName("zona") val zona: String?,
    @SerializedName("direccion") val direccion: String?,
    @SerializedName("cp") val cp: String?,
    @SerializedName("localidad") val localidad: String?,
    /** Lista bajo la clave JSON singular `"telefono"`. */
    @SerializedName("telefono") val telefonos: List<String>? = null,
    @SerializedName("email") val emails: List<String>? = null,
    @SerializedName("web") val web: String? = null,
)
