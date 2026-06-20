package com.example.empresas_turismo_activo.network

import com.example.empresas_turismo_activo.data.model.Empresa
import retrofit2.http.GET

data class CatalogoResponse(
    val total: Int = 0,
    val empresas: List<Empresa> = emptyList(),
)

interface WebService {

    @GET("nachorg04/12b2505ad79acb9f662a1d9cb694027e/raw/4c6818f9dc55b6c9a854f7c1fd0751e09a7191a2/gistfile1.txt")
    suspend fun obtenerCatalogoEmpresas(): CatalogoResponse
}
