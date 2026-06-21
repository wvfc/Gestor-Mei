package com.gestormei.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gestormei.data.model.Despesa
import kotlinx.coroutines.flow.Flow

@Dao
interface DespesaDao {

    @Query("SELECT * FROM despesas WHERE empresaId = :empresaId ORDER BY data DESC, id DESC")
    fun observarPorEmpresa(empresaId: Long): Flow<List<Despesa>>

    @Insert
    suspend fun inserir(despesa: Despesa): Long

    @Update
    suspend fun atualizar(despesa: Despesa)

    @Delete
    suspend fun excluir(despesa: Despesa)

    @Query("SELECT * FROM despesas")
    suspend fun todas(): List<Despesa>

    @Query("SELECT referencia FROM despesas WHERE empresaId = :empresaId AND referencia <> ''")
    suspend fun referencias(empresaId: Long): List<String>

    @Query("DELETE FROM despesas WHERE empresaId = :empresaId")
    suspend fun excluirPorEmpresa(empresaId: Long)
}
