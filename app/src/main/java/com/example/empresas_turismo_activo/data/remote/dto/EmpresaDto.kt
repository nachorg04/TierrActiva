package com.example.empresas_turismo_activo.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EmpresaDto(
    @SerializedName("id") val id: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("contacto") val contacto: ContactoDto?,
    @SerializedName("coordenadas") val coordenadas: CoordenadasDto?,
    /** Objeto dinámico (facebook, instagram, …) que se aplana después a List<RedSocial>. */
    @SerializedName("redes_sociales") val redesSociales: Map<String, String>? = null,
    @SerializedName("imagen_portada") val imagenPortada: String?,
    @SerializedName("informacion") val informacion: InformacionDto?,
)

