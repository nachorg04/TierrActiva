package com.example.empresas_turismo_activo.data.local.converter

import androidx.room.TypeConverter
import com.example.empresas_turismo_activo.data.model.Actividad
import com.example.empresas_turismo_activo.data.model.RedSocial
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Convierte estructuras de lista a texto JSON y viceversa usando Moshi.
 * Ideal para Kotlin porque respeta la nulabilidad de las variables (Null-Safety).
 */
class EmpresaConverters {

    // 1. Preparamos nuestro traductor Moshi con soporte nativo para Kotlin
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // 2. Creamos los "Moldes" (Adaptadores) para cada tipo exacto de lista que tenemos
    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    private val redesListType = Types.newParameterizedType(List::class.java, RedSocial::class.java)
    private val redesListAdapter = moshi.adapter<List<RedSocial>>(redesListType)

    private val actividadesListType = Types.newParameterizedType(List::class.java, Actividad::class.java)
    private val actividadesListAdapter = moshi.adapter<List<Actividad>>(actividadesListType)

    // =====================================================================
    // CONVERSORES PARA LISTAS DE TEXTO (Teléfonos y Emails)
    // =====================================================================

    @TypeConverter
    fun stringListFromJson(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        // Usamos el molde para leer el JSON y convertirlo en Lista
        return stringListAdapter.fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun stringListToJson(list: List<String>?): String {
        // Usamos el molde para aplastar la Lista y convertirla en JSON
        return stringListAdapter.toJson(list.orEmpty())
    }

    // =====================================================================
    // CONVERSORES PARA REDES SOCIALES
    // =====================================================================

    @TypeConverter
    fun redesFromJson(value: String?): List<RedSocial> {
        if (value.isNullOrBlank()) return emptyList()
        return redesListAdapter.fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun redesToJson(list: List<RedSocial>?): String {
        return redesListAdapter.toJson(list.orEmpty())
    }

    // =====================================================================
    // CONVERSORES PARA ACTIVIDADES
    // =====================================================================

    @TypeConverter
    fun actividadesFromJson(value: String?): List<Actividad> {
        if (value.isNullOrBlank()) return emptyList()
        return actividadesListAdapter.fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun actividadesToJson(list: List<Actividad>?): String {
        return actividadesListAdapter.toJson(list.orEmpty())
    }
}