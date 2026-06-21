package com.gestormei.data.repository

import com.gestormei.data.dao.EmpresaDao
import com.gestormei.data.model.Empresa
import kotlinx.coroutines.flow.Flow

class EmpresaRepository(private val dao: EmpresaDao) {

    fun observarTodas(): Flow<List<Empresa>> = dao.observarTodas()

    fun observarPorId(id: Long): Flow<Empresa?> = dao.observarPorId(id)

    suspend fun contar(): Int = dao.contar()

    suspend fun inserir(empresa: Empresa): Long = dao.inserir(empresa)

    suspend fun atualizar(empresa: Empresa) = dao.atualizar(empresa)

    suspend fun excluir(empresa: Empresa) = dao.excluir(empresa)
}
