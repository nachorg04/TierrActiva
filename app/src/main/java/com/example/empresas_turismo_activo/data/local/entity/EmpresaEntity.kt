package com.example.empresas_turismo_activo.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "empresas_table",
    indices = [Index(value = ["localidad"])],
)
data class EmpresaEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val localidad: String,
    val direccion: String?,
    val lat: Double,
    val lng: Double,
    val imagenPortada: String,
)
