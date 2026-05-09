package com.example.empresas_turismo_activo.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.empresas_turismo_activo.data.local.converter.EmpresaConverters
import com.example.empresas_turismo_activo.data.local.dao.EmpresaDao
import com.example.empresas_turismo_activo.data.local.entity.EmpresaEntity

/**
 * Base de datos Room principal; versiona de forma explícita y deja exportSchema en false en prototipos para
 * evitar ruido de CI hasta que se formalicen migraciones.
 */
@Database(
    entities = [EmpresaEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(EmpresaConverters::class)
abstract class AppDatabase : RoomDatabase() {

    /** Punto de acceso al DAO de empresas. */
    abstract fun empresaDao(): EmpresaDao

    companion object {
        private const val DATABASE_NAME = "empresas_turismo_activo.db"

        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Devuelve instancia singleton thread-safe; el contexto debe ser applicationContext del proceso Android.
         */
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME,
                ).build().also { instance = it }
            }
        }
    }
}
