package com.gestormei.data.repository

import com.gestormei.data.dao.ClienteDao
import com.gestormei.data.model.Cliente
import kotlinx.coroutines.flow.Flow

class ClienteRepository(private val dao: ClienteDao) {

    fun observarPorEmpresa(empresaId: Long): Flow<List<Cliente>> =
        dao.observarPorEmpresa(empresaId)

    suspend fun salvar(cliente: Cliente) {
        if (cliente.id == 0L) dao.inserir(cliente) else dao.atualizar(cliente)
    }

    suspend fun inserirVarios(clientes: List<Cliente>) = dao.inserirVarios(clientes)

    suspend fun excluir(cliente: Cliente) = dao.excluir(cliente)

    suspend fun excluirTodos(empresaId: Long) = dao.excluirPorEmpresa(empresaId)
}
