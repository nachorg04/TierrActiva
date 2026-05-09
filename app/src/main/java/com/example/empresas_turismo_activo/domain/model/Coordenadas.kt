package com.example.empresas_turismo_activo.domain.model

/**
 * Posición geográfica de una empresa de turismo activo.
 *
 * @property lat Latitud en grados decimales (WGS84).
 * @property lng Longitud en grados decimales (WGS84).
 */
data class Coordenadas(
    val lat: Double,
    val lng: Double,
)

/** Evita ordenar contra (0,0) típico de datos ausentes mal normalizados. */
fun Coordenadas.isLikelyGeocoded(): Boolean =
    lat in -90.0..90.0 &&
        lng in -180.0..180.0 &&
        !(kotlin.math.abs(lat) < 1e-7 && kotlin.math.abs(lng) < 1e-7)
