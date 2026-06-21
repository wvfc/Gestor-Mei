package com.gestormei.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gestormei.data.model.Cliente
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {

    @Query("SELECT * FROM clientes WHERE empresaId = :empresaId ORDER BY nome")
    fun observarPorEmpresa(empresaId: Long): Flow<List<Cliente>>

    @Insert
    suspend fun inserir(cliente: Cliente): Long

    @Insert
    suspend fun inserirVarios(clientes: List<Cliente>)

    @Update
    suspend fun atualizar(cliente: Cliente)

    @Delete
    suspend fun excluir(cliente: Cliente)
}
