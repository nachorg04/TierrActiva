package com.example.empresas_turismo_activo.data.model

fun Empresa.normalizar(): Empresa? {
    val idLimpio = id.noVacio() ?: return null
    val contactoN = contacto ?: Contacto()
    val coordenadasN = coordenadas ?: Coordenadas()
    val informacionN = informacion ?: Informacion()
    return copy(
        id = idLimpio,
        nombre = nombre.oVacio(),
        imagenPortada = imagenPortada.oVacio(),
        contacto = contactoN.copy(
            concejo = contactoN.concejo.oVacio().let { if (it.contains("?")) "" else it },
            localidad = contactoN.localidad.oVacio().let { if (it.contains("?")) "" else it },
            telefonos = contactoN.telefonos.orEmpty(),
            emails = contactoN.emails.orEmpty(),
            redesSociales = contactoN.redesSociales.orEmpty(),
            web = contactoN.web.noVacio(),
            direccion = contactoN.construirDireccionMultilinea(),
        ),
        coordenadas = coordenadasN.copy(lat = coordenadasN.lat ?: 0.0, lng = coordenadasN.lng ?: 0.0),
        informacion = informacionN.copy(
            titulo = informacionN.titulo.oVacio(),
            zonaActividad = informacionN.zonaActividad.oVacio(),
            actividades = informacionN.actividades.orEmpty(),
            descripcion = informacionN.construirDescripcionCompleta(),
        ),
    )
}

private fun Contacto.construirDireccionMultilinea(): String? {
    val lineas = listOfNotNull(
        direccion.noVacio(),
        cp.noVacio()?.let { "CP $it" },
        zona.noVacio()?.let { "Zona: $it" },
    )
    return lineas.joinToString("\n").ifEmpty { null }
}

private fun Informacion.construirDescripcionCompleta(): String {
    val base = descripcion.oVacio()
    val serv = servicios.noVacio()?.let { "\n\nServicios:\n$it" }.orEmpty()
    return (base + serv).ifEmpty { titulo.oVacio() }
}

private fun String?.oVacio(): String = this.orEmpty().trim()
private fun String?.noVacio(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
