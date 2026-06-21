package com.gestormei.data.repository

import com.gestormei.data.dao.CompromissoDao
import com.gestormei.data.model.Compromisso
import kotlinx.coroutines.flow.Flow

class AgendaRepository(private val dao: CompromissoDao) {

    fun observarPorEmpresa(empresaId: Long): Flow<List<Compromisso>> =
        dao.observarPorEmpresa(empresaId)

    suspend fun salvar(compromisso: Compromisso) {
        if (compromisso.id == 0L) dao.inserir(compromisso) else dao.atualizar(compromisso)
    }

    suspend fun excluir(compromisso: Compromisso) = dao.excluir(compromisso)
}
