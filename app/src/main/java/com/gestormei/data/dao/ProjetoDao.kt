package com.gestormei.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gestormei.data.model.Projeto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjetoDao {

    @Query("SELECT * FROM projetos WHERE empresaId = :empresaId ORDER BY id DESC")
    fun observarPorEmpresa(empresaId: Long): Flow<List<Projeto>>

    @Insert
    suspend fun inserir(projeto: Projeto): Long

    @Update
    suspend fun atualizar(projeto: Projeto)

    @Delete
    suspend fun excluir(projeto: Projeto)

    @Query("DELETE FROM projetos WHERE empresaId = :empresaId")
    suspend fun excluirPorEmpresa(empresaId: Long)
}
