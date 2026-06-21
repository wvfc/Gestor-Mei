package com.gestormei.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gestormei.data.dao.ClienteDao
import com.gestormei.data.dao.CompromissoDao
import com.gestormei.data.dao.DespesaDao
import com.gestormei.data.dao.EmailDao
import com.gestormei.data.dao.EmpresaDao
import com.gestormei.data.dao.IdeiaDao
import com.gestormei.data.dao.ProjetoDao
import com.gestormei.data.dao.ReceitaDao
import com.gestormei.data.dao.SenhaDao
import com.gestormei.data.model.Cliente
import com.gestormei.data.model.Compromisso
import com.gestormei.data.model.Despesa
import com.gestormei.data.model.EmailRegistro
import com.gestormei.data.model.Empresa
import com.gestormei.data.model.Ideia
import com.gestormei.data.model.Projeto
import com.gestormei.data.model.Receita
import com.gestormei.data.model.Senha

@Database(
    entities = [
        Empresa::class,
        Receita::class,
        Despesa::class,
        EmailRegistro::class,
        Senha::class,
        Compromisso::class,
        Projeto::class,
        Ideia::class,
        Cliente::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun empresaDao(): EmpresaDao
    abstract fun receitaDao(): ReceitaDao
    abstract fun despesaDao(): DespesaDao
    abstract fun emailDao(): EmailDao
    abstract fun senhaDao(): SenhaDao
    abstract fun compromissoDao(): CompromissoDao
    abstract fun projetoDao(): ProjetoDao
    abstract fun ideiaDao(): IdeiaDao
    abstract fun clienteDao(): ClienteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Preserva os dados existentes ao adicionar a coluna contaNoLimite. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE receitas ADD COLUMN contaNoLimite INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gestor_mei.db"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
