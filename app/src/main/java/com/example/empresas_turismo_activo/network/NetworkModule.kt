package com.example.empresas_turismo_activo.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {

    private const val BASE_URL = "https://gist.githubusercontent.com/"

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(RedSocialListAdapter())
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val webService: WebService by lazy {
        retrofit.create(WebService::class.java)
    }
}
