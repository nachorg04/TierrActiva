package com.example.empresas_turismo_activo.domain.model

/**
 * Representa una empresa de turismo activo en Asturias con todos sus datos anidados expuestos
 * desde la capa de dominio como una única pieza coherente.
 *
 * @property id Identificador estable en la fuente de datos original.
 * @property nombre Razón social o nombre comercial de la empresa.
 * @property contacto Datos territoriales y de comunicación normalizados.
 * @property coordenadas Coordenadas geográficas de referencia para mapas y distancias.
 * @property imagenPortada URL de la cabecera o imagen principal de la empresa.
 * @property informacion Contenidos descriptivos y listado detallado de actividades asociadas.
 */
data class Empresa(
    val id: String,
    val nombre: String,
    val contacto: Contacto,
    val coordenadas: Coordenadas,
    val imagenPortada: String,
    val informacion: Informacion,
)
