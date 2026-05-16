package com.example.empresas_turismo_activo.data.model

/**
 * Enlace o perfil de red social relacionado con el contacto de la empresa.
 *
 * @property plataforma Nombre visible de la plataforma (Instagram, Facebook, etc.).
 * @property url Dirección URL del recurso.
 */
data class RedSocial(
    val plataforma: String,
    val url: String,
)
