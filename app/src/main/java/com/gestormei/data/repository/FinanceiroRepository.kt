package com.gestormei.data.repository

import com.gestormei.data.dao.DespesaDao
import com.gestormei.data.dao.ReceitaDao
import com.gestormei.data.model.Despesa
import com.gestormei.data.model.Receita
import kotlinx.coroutines.flow.Flow

class FinanceiroRepository(
    private val receitaDao: ReceitaDao,
    private val despesaDao: DespesaDao
) {

    fun observarReceitas(empresaId: Long): Flow<List<Receita>> =
        receitaDao.observarPorEmpresa(empresaId)

    fun observarDespesas(empresaId: Long): Flow<List<Despesa>> =
        despesaDao.observarPorEmpresa(empresaId)

    suspend fun salvarReceita(receita: Receita) {
        if (receita.id == 0L) receitaDao.inserir(receita) else receitaDao.atualizar(receita)
    }

    suspend fun excluirReceita(receita: Receita) = receitaDao.excluir(receita)

    suspend fun salvarDespesa(despesa: Despesa) {
        if (despesa.id == 0L) despesaDao.inserir(despesa) else despesaDao.atualizar(despesa)
    }

    suspend fun excluirDespesa(despesa: Despesa) = despesaDao.excluir(despesa)
}
