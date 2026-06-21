package com.gestormei.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gestormei.GestorMeiApplication
import com.gestormei.data.model.Despesa
import com.gestormei.data.model.Receita
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceiroViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as GestorMeiApplication).container

    private val empresaId: StateFlow<Long?> = container.empresaAtivaId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val receitas: StateFlow<List<Receita>> = empresaId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else container.financeiroRepository.observarReceitas(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val despesas: StateFlow<List<Despesa>> = empresaId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else container.financeiroRepository.observarDespesas(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val empresaAtivaId: StateFlow<Long?> = empresaId

    fun salvarReceita(receita: Receita) {
        val id = empresaId.value ?: return
        viewModelScope.launch {
            container.financeiroRepository.salvarReceita(receita.copy(empresaId = id))
        }
    }

    fun excluirReceita(receita: Receita) {
        viewModelScope.launch { container.financeiroRepository.excluirReceita(receita) }
    }

    fun salvarDespesa(despesa: Despesa) {
        val id = empresaId.value ?: return
        viewModelScope.launch {
            container.financeiroRepository.salvarDespesa(despesa.copy(empresaId = id))
        }
    }

    fun excluirDespesa(despesa: Despesa) {
        viewModelScope.launch { container.financeiroRepository.excluirDespesa(despesa) }
    }
}
