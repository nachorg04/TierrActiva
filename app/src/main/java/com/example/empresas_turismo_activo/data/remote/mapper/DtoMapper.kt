package com.example.empresas_turismo_activo.data.remote.mapper

import com.example.empresas_turismo_activo.data.remote.dto.ActividadDto
import com.example.empresas_turismo_activo.data.remote.dto.ContactoDto
import com.example.empresas_turismo_activo.data.remote.dto.CoordenadasDto
import com.example.empresas_turismo_activo.data.remote.dto.EmpresaDto
import com.example.empresas_turismo_activo.data.remote.dto.InformacionDto
import com.example.empresas_turismo_activo.data.model.Actividad
import com.example.empresas_turismo_activo.data.model.Contacto
import com.example.empresas_turismo_activo.data.model.Coordenadas
import com.example.empresas_turismo_activo.data.model.Empresa
import com.example.empresas_turismo_activo.data.model.Informacion
import com.example.empresas_turismo_activo.data.model.RedSocial
import java.util.Locale

/** Conversión estable desde capa red hacia modelo de dominio usado también por Room. */

fun EmpresaDto.toDomain(): Empresa? {
    val empresaId = id?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val redes = redesSociales.toRedSocialList()
    val contactoDomain = contacto?.toDomain(redes)
        ?: Contacto(
            concejo = "",
            direccion = null,
            localidad = "",
            telefonos = emptyList(),
            emails = emptyList(),
            web = null,
            redesSociales = redes,
        )
    return Empresa(
        id = empresaId,
        nombre = nombre.orEmpty(),
        contacto = contactoDomain,
        coordenadas = coordenadas.toDomain(),
        imagenPortada = imagenPortada.orEmpty(),
        informacion = informacion.toDomain(),
    )
}

private fun Map<String, String>?.toRedSocialList(): List<RedSocial> =
    this.orEmpty()
        .entries
        .sortedBy { it.key }
        .map { (plataforma, url) ->
            RedSocial(
                plataforma = plataforma.replaceFirstChar { ch ->
                    if (ch.isLowerCase()) ch.titlecase(Locale.ROOT) else ch.toString()
                },
                url = url.trim(),
            )
        }

private fun ContactoDto.toDomain(redesSocialesEmpresa: List<RedSocial>): Contacto = Contacto(
    concejo = concejo.orEmpty(),
    direccion = buildDireccionMultilinea(),
    localidad = localidad.orEmpty(),
    telefonos = telefonos.orEmpty(),
    emails = emails.orEmpty(),
    web = web?.trim()?.takeUnless { it.isEmpty() },
    redesSociales = redesSocialesEmpresa,
)

/** Compone dirección, código postal y zona geográfica en un solo bloque legible en UI. */
private fun ContactoDto.buildDireccionMultilinea(): String? =
    buildString {
        direccion?.trim()?.takeIf { it.isNotEmpty() }?.let { appendLine(it) }
        cp?.trim()?.takeIf { it.isNotEmpty() }?.let { append("CP ").appendLine(it) }
        zona?.trim()?.takeIf { it.isNotEmpty() }?.let { append("Zona: ").appendLine(it) }
    }.trim().ifEmpty { null }

private fun CoordenadasDto?.toDomain(): Coordenadas =
    Coordenadas(lat = this?.lat ?: 0.0, lng = this?.lng ?: 0.0)

private fun InformacionDto?.toDomain(): Informacion {
    if (this == null) {
        return Informacion(
            titulo = "",
            descripcion = "",
            zonaActividad = "",
            actividades = emptyList(),
        )
    }
    val descripcionCompleta = buildString {
        append(descripcion?.trim().orEmpty())
        val servTrim = servicios?.trim().orEmpty()
        if (servTrim.isNotEmpty()) {
            if (isNotEmpty()) append("\n\n")
            append("Servicios:\n").append(servTrim)
        }
    }.trim()
    val tituloSeguro = titulo.orEmpty()
    return Informacion(
        titulo = tituloSeguro,
        descripcion = descripcionCompleta.ifEmpty { tituloSeguro },
        zonaActividad = zonaActividad.orEmpty(),
        actividades = actividades.orEmpty().map { it.toDomain() },
    )
}

private fun ActividadDto.toDomain(): Actividad = Actividad(
    nombre = nombre.orEmpty(),
    imagenUrl = imagenUrl.orEmpty(),
    categoria = categoria.orEmpty(),
)
