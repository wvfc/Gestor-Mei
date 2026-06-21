package com.gestormei.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gestormei.data.model.Compromisso
import kotlinx.coroutines.flow.Flow

@Dao
interface CompromissoDao {

    @Query("SELECT * FROM compromissos WHERE empresaId = :empresaId ORDER BY data, hora")
    fun observarPorEmpresa(empresaId: Long): Flow<List<Compromisso>>

    @Insert
    suspend fun inserir(compromisso: Compromisso): Long

    @Update
    suspend fun atualizar(compromisso: Compromisso)

    @Delete
    suspend fun excluir(compromisso: Compromisso)

    @Query("DELETE FROM compromissos WHERE empresaId = :empresaId")
    suspend fun excluirPorEmpresa(empresaId: Long)
}
