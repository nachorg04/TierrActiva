package com.example.empresas_turismo_activo.domain.model

/**
 * Contenido descriptivo y actividades agrupadas bajo información de empresa.
 *
 * @property titulo Título destacado del bloque informativo.
 * @property descripcion Texto descriptivo ampliado.
 * @property zonaActividad Zona geográfica o comercial donde se desarrolla la actividad.
 * @property actividades Listado de actividades enlazadas a este bloque.
 */
data class Informacion(
    val titulo: String,
    val descripcion: String,
    val zonaActividad: String,
    val actividades: List<Actividad> = emptyList(),
)
