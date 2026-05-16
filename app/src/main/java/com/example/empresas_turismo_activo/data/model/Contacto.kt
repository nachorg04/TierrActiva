package com.example.empresas_turismo_activo.data.model

/**
 * Datos de ubicación y contacto de una empresa.
 *
 * @property concejo Concejo asturiano o equivalente administrativo.
 * @property direccion Dirección postal completa cuando exista en la fuente.
 * @property localidad Localidad o núcleo de población (útil para búsquedas).
 * @property telefonos Números de teléfono asociados; puede provenir de un único campo en JSON.
 * @property emails Direcciones de correo electrónico; puede provenir de un único campo en JSON.
 * @property web Página principal u otra URL general de contacto.
 * @property redesSociales Enlaces específicos a redes sociales, si la fuente los incluye.
 */
data class Contacto(
    val concejo: String,
    val direccion: String?,
    val localidad: String,
    val telefonos: List<String> = emptyList(),
    val emails: List<String> = emptyList(),
    val web: String?,
    val redesSociales: List<RedSocial> = emptyList(),
)
