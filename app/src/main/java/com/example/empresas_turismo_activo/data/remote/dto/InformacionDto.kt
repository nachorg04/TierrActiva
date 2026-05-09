package com.example.empresas_turismo_activo.data.remote.dto

import com.google.gson.annotations.SerializedName

data class InformacionDto(
    @SerializedName("titulo") val titulo: String?,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("servicios") val servicios: String?,
    @SerializedName("zona_actividad") val zonaActividad: String?,
    @SerializedName("actividades") val actividades: List<ActividadDto>? = null,
)
