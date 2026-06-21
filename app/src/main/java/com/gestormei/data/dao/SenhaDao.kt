package com.gestormei.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gestormei.data.model.Senha
import kotlinx.coroutines.flow.Flow

@Dao
interface SenhaDao {

    @Query("SELECT * FROM senhas WHERE empresaId = :empresaId ORDER BY nomeServico, id DESC")
    fun observarPorEmpresa(empresaId: Long): Flow<List<Senha>>

    @Insert
    suspend fun inserir(senha: Senha): Long

    @Update
    suspend fun atualizar(senha: Senha)

    @Delete
    suspend fun excluir(senha: Senha)

    @Query("DELETE FROM senhas WHERE empresaId = :empresaId")
    suspend fun excluirPorEmpresa(empresaId: Long)
}
