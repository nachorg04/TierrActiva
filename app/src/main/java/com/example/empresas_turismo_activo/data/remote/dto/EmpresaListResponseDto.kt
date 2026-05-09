package com.example.empresas_turismo_activo.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Raíz JSON: metadatos de conteo y listado persistible de empresas. */
data class EmpresaListResponseDto(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("empresas") val empresas: List<EmpresaDto> = emptyList(),
)
