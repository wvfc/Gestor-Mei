package com.gestormei.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gestormei.data.model.EmailRegistro
import kotlinx.coroutines.flow.Flow

@Dao
interface EmailDao {

    @Query("SELECT * FROM emails WHERE empresaId = :empresaId ORDER BY servico, id DESC")
    fun observarPorEmpresa(empresaId: Long): Flow<List<EmailRegistro>>

    @Insert
    suspend fun inserir(email: EmailRegistro): Long

    @Update
    suspend fun atualizar(email: EmailRegistro)

    @Delete
    suspend fun excluir(email: EmailRegistro)

    @Query("DELETE FROM emails WHERE empresaId = :empresaId")
    suspend fun excluirPorEmpresa(empresaId: Long)
}
