package com.gestormei.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gestormei.data.model.Ideia
import kotlinx.coroutines.flow.Flow

@Dao
interface IdeiaDao {

    @Query("SELECT * FROM ideias WHERE empresaId = :empresaId ORDER BY id DESC")
    fun observarPorEmpresa(empresaId: Long): Flow<List<Ideia>>

    @Insert
    suspend fun inserir(ideia: Ideia): Long

    @Update
    suspend fun atualizar(ideia: Ideia)

    @Delete
    suspend fun excluir(ideia: Ideia)

    @Query("DELETE FROM ideias WHERE empresaId = :empresaId")
    suspend fun excluirPorEmpresa(empresaId: Long)
}
