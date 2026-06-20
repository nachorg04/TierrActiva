package com.example.empresas_turismo_activo.data.model

import com.squareup.moshi.Json

data class Contacto(
    val concejo: String? = null,
    val zona: String? = null,
    val direccion: String? = null,
    val cp: String? = null,
    val localidad: String? = null,
    @Json(name = "telefono") val telefonos: List<String>? = null,
    @Json(name = "email") val emails: List<String>? = null,
    val web: String? = null,
    @Json(name = "redes_sociales") val redesSociales: List<RedSocial>? = null,
)
