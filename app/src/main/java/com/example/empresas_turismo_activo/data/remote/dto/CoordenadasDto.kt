package com.example.empresas_turismo_activo.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CoordenadasDto(
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lng") val lng: Double?,
)
