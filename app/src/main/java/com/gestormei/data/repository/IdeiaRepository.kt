package com.gestormei.data.repository

import com.gestormei.data.dao.IdeiaDao
import com.gestormei.data.model.Ideia
import kotlinx.coroutines.flow.Flow

class IdeiaRepository(private val dao: IdeiaDao) {

    fun observarPorEmpresa(empresaId: Long): Flow<List<Ideia>> =
        dao.observarPorEmpresa(empresaId)

    suspend fun salvar(ideia: Ideia) {
        if (ideia.id == 0L) dao.inserir(ideia) else dao.atualizar(ideia)
    }

    suspend fun excluir(ideia: Ideia) = dao.excluir(ideia)
}
