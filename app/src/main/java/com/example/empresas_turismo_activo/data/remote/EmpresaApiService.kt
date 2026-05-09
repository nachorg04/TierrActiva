package com.example.empresas_turismo_activo.data.remote

import com.example.empresas_turismo_activo.data.remote.dto.EmpresaListResponseDto
import retrofit2.http.GET

/**
 * Cliente declarativo; el path relativo se resuelve contra la base HTTPS configurada en Retrofit.
 */
interface EmpresaApiService {

    @GET("nachorg04/12b2505ad79acb9f662a1d9cb694027e/raw/4c6818f9dc55b6c9a854f7c1fd0751e09a7191a2/gistfile1.txt")
    suspend fun getEmpresaCatalogo(): EmpresaListResponseDto
}
