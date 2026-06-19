package com.example.empresas_turismo_activo.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.empresas_turismo_activo.data.model.Actividad
import com.example.empresas_turismo_activo.data.model.RedSocial

/**
 * Fila única de la tabla física donde se aplana cada [com.example.empresas_turismo_activo.data.model.Empresa];
 * colecciones anidadas se serializan a JSON mediante los convertidores registrados en la base de datos Room.
 */
@Entity(
    tableName = "empresas_table",
    indices = [
        Index(value = ["localidad"]),
        Index(value = ["zona_actividad"]),
    ],
)
data class EmpresaEntity(
    @PrimaryKey
    val id: String, // Room asume que la columna es "id"

    val nombre: String,
    val concejo: String,
    val direccion: String?,
    val localidad: String,
    val telefonos: List<String>,
    val emails: List<String>,
    val web: String?,
    val lat: Double,
    val lng: Double,
    val actividades: List<Actividad>,

    // ==========================================================
    // EXCEPCIONES: Mapeos de camelCase (Kotlin) a snake_case (SQL)
    // ==========================================================
    @ColumnInfo(name = "redes_sociales")
    val redesSociales: List<RedSocial>,

    @ColumnInfo(name = "imagen_portada")
    val imagenPortada: String,

    @ColumnInfo(name = "titulo_informacion")
    val tituloInformacion: String,

    @ColumnInfo(name = "descripcion_informacion")
    val descripcionInformacion: String,

    @ColumnInfo(name = "zona_actividad")
    val zonaActividad: String,
)