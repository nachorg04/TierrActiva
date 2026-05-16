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
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "nombre")
    val nombre: String,
    @ColumnInfo(name = "concejo")
    val concejo: String,
    @ColumnInfo(name = "direccion")
    val direccion: String?,
    @ColumnInfo(name = "localidad")
    val localidad: String,
    @ColumnInfo(name = "telefonos")
    val telefonos: List<String>,
    @ColumnInfo(name = "emails")
    val emails: List<String>,
    @ColumnInfo(name = "redes_sociales")
    val redesSociales: List<RedSocial>,
    @ColumnInfo(name = "web")
    val web: String?,
    @ColumnInfo(name = "lat")
    val lat: Double,
    @ColumnInfo(name = "lng")
    val lng: Double,
    @ColumnInfo(name = "imagen_portada")
    val imagenPortada: String,
    @ColumnInfo(name = "titulo_informacion")
    val tituloInformacion: String,
    @ColumnInfo(name = "descripcion_informacion")
    val descripcionInformacion: String,
    @ColumnInfo(name = "zona_actividad")
    val zonaActividad: String,
    @ColumnInfo(name = "actividades")
    val actividades: List<Actividad>,
)
