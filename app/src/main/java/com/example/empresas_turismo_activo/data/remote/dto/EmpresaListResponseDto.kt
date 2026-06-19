package com.example.empresas_turismo_activo.data.remote.dto

/** Raíz JSON: metadatos de conteo y listado persistible de empresas. */
data class EmpresaListResponseDto(
    val total: Int = 0,
    val empresas: List<EmpresaDto> = emptyList(),
)
