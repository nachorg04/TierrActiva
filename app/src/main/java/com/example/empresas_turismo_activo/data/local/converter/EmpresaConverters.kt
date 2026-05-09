package com.example.empresas_turismo_activo.data.local.converter

import androidx.room.TypeConverter
import com.example.empresas_turismo_activo.domain.model.Actividad
import com.example.empresas_turismo_activo.domain.model.RedSocial
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Convierte estructuras de lista u objetos anidados a texto JSON y viceversa para columnas ROOM únicas,
 * usando Gson como formato estable almacenado en la tabla física empresas_table.
 */
class EmpresaConverters {

    private val gson = Gson()

    /**
     * Persiste colecciones de cadenas (teléfonos o correos); comparten conversor porque JVM ve el mismo tipo
     * subyacente y Room solo permite una pareja entrada/salida por firma JVM.
     */
    @TypeConverter
    fun stringListFromJson(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun stringListToJson(list: List<String>?): String = gson.toJson(list.orEmpty())

    /** Serializa objetos RedSocial cuando la empresa publica redes adicionales. */
    @TypeConverter
    fun redesFromJson(value: String?): List<RedSocial> {
        if (value.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<RedSocial>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun redesToJson(list: List<RedSocial>?): String = gson.toJson(list.orEmpty())

    /** Guarda el listado de actividades respetando claves JSON de redacción del API (p. ej. imagen_url). */
    @TypeConverter
    fun actividadesFromJson(value: String?): List<Actividad> {
        if (value.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<Actividad>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun actividadesToJson(list: List<Actividad>?): String = gson.toJson(list.orEmpty())
}
