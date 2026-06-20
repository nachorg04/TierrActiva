package com.example.empresas_turismo_activo.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GeoDistance {

    private const val EARTH_RADIUS_M = 6_371_000.0

    fun metrosEntre(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val φ1 = Math.toRadians(lat1)
        val φ2 = Math.toRadians(lat2)
        val Δφ = Math.toRadians(lat2 - lat1)
        val Δλ = Math.toRadians(lng2 - lng1)
        val sinDPhi = sin(Δφ / 2)
        val sinDLambda = sin(Δλ / 2)
        val a = sinDPhi * sinDPhi +
            cos(φ1) * cos(φ2) * sinDLambda * sinDLambda
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_M * c
    }
}
