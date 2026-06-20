package com.example.empresas_turismo_activo.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.empresas_turismo_activo.data.local.dao.EmpresaDao
import com.example.empresas_turismo_activo.data.local.entity.EmpresaEntity

@Database(
    entities = [EmpresaEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun empresaDao(): EmpresaDao

    companion object {
        private const val DATABASE_NAME = "empresas_turismo_activo.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun obtenerInstancia(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME,
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }
}
