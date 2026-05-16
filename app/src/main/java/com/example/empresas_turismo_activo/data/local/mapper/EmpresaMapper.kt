package com.example.empresas_turismo_activo.data.local.mapper

import com.example.empresas_turismo_activo.data.local.entity.EmpresaEntity
import com.example.empresas_turismo_activo.data.model.Contacto
import com.example.empresas_turismo_activo.data.model.Coordenadas
import com.example.empresas_turismo_activo.data.model.Empresa
import com.example.empresas_turismo_activo.data.model.Informacion

/** Transformaciones puras entre fila Room y modelo de dominio sin efectos colaterales. */

/** Reconstruye el gráfico de dominio a partir de columnas aplanadas y listas ya materializadas en memoria. */
fun EmpresaEntity.toDomain(): Empresa = Empresa(
    id = id,
    nombre = nombre,
    contacto = Contacto(
        concejo = concejo,
        direccion = direccion,
        localidad = localidad,
        telefonos = telefonos,
        emails = emails,
        web = web,
        redesSociales = redesSociales,
    ),
    coordenadas = Coordenadas(lat = lat, lng = lng),
    imagenPortada = imagenPortada,
    informacion = Informacion(
        titulo = tituloInformacion,
        descripcion = descripcionInformacion,
        zonaActividad = zonaActividad,
        actividades = actividades,
    ),
)

/** Aplana cada empresa de dominio a la firma física conocida por el DAO antes de ejecutar escrituras locales. */
fun Empresa.toEntity(): EmpresaEntity = EmpresaEntity(
    id = id,
    nombre = nombre,
    concejo = contacto.concejo,
    direccion = contacto.direccion,
    localidad = contacto.localidad,
    telefonos = contacto.telefonos,
    emails = contacto.emails,
    redesSociales = contacto.redesSociales,
    web = contacto.web,
    lat = coordenadas.lat,
    lng = coordenadas.lng,
    imagenPortada = imagenPortada,
    tituloInformacion = informacion.titulo,
    descripcionInformacion = informacion.descripcion,
    zonaActividad = informacion.zonaActividad,
    actividades = informacion.actividades,
)
