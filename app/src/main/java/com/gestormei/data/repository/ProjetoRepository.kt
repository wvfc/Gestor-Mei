package com.gestormei.data.repository

import com.gestormei.data.dao.ProjetoDao
import com.gestormei.data.model.Projeto
import kotlinx.coroutines.flow.Flow

class ProjetoRepository(private val dao: ProjetoDao) {

    fun observarPorEmpresa(empresaId: Long): Flow<List<Projeto>> =
        dao.observarPorEmpresa(empresaId)

    suspend fun salvar(projeto: Projeto) {
        if (projeto.id == 0L) dao.inserir(projeto) else dao.atualizar(projeto)
    }

    suspend fun excluir(projeto: Projeto) = dao.excluir(projeto)
}
