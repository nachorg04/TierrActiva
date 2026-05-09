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
