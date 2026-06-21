package com.gestormei.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gestormei.data.model.Receita
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceitaDao {

    @Query("SELECT * FROM receitas WHERE empresaId = :empresaId ORDER BY data DESC, id DESC")
    fun observarPorEmpresa(empresaId: Long): Flow<List<Receita>>

    @Insert
    suspend fun inserir(receita: Receita): Long

    @Update
    suspend fun atualizar(receita: Receita)

    @Delete
    suspend fun excluir(receita: Receita)

    @Query("DELETE FROM receitas WHERE empresaId = :empresaId")
    suspend fun excluirPorEmpresa(empresaId: Long)
}
