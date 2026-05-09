package com.example.empresas_turismo_activo.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ActividadDto(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("imagen_url") val imagenUrl: String?,
    @SerializedName("categoria") val categoria: String?,
)
