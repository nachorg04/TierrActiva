package com.example.empresas_turismo_activo.data.model

data class Coordenadas(
    val lat: Double? = null,
    val lng: Double? = null,
)

fun Coordenadas.esProbablementeGeocodificado(): Boolean =
    (lat ?: 0.0) in -90.0..90.0 &&
        (lng ?: 0.0) in -180.0..180.0 &&
        !(kotlin.math.abs(lat ?: 0.0) < 1e-7 && kotlin.math.abs(lng ?: 0.0) < 1e-7)
