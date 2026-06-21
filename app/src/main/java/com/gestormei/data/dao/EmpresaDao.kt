package com.gestormei.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gestormei.data.model.Empresa
import kotlinx.coroutines.flow.Flow

@Dao
interface EmpresaDao {

    @Query("SELECT * FROM empresas ORDER BY id")
    fun observarTodas(): Flow<List<Empresa>>

    @Query("SELECT * FROM empresas WHERE id = :id")
    fun observarPorId(id: Long): Flow<Empresa?>

    @Query("SELECT COUNT(*) FROM empresas")
    suspend fun contar(): Int

    @Query("SELECT * FROM empresas")
    suspend fun todas(): List<Empresa>

    @androidx.room.Insert
    suspend fun inserirVarios(empresas: List<Empresa>)

    @Insert
    suspend fun inserir(empresa: Empresa): Long

    @Update
    suspend fun atualizar(empresa: Empresa)

    @Delete
    suspend fun excluir(empresa: Empresa)
}
