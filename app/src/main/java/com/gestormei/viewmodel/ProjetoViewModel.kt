package com.gestormei.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gestormei.GestorMeiApplication
import com.gestormei.data.model.Projeto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProjetoViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as GestorMeiApplication).container

    private val empresaId: StateFlow<Long?> = container.empresaAtivaId.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    val empresaAtivaId: StateFlow<Long?> = empresaId

    val projetos: StateFlow<List<Projeto>> = empresaId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else container.projetoRepository.observarPorEmpresa(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun salvar(projeto: Projeto) {
        val id = empresaId.value ?: return
        viewModelScope.launch {
            container.projetoRepository.salvar(projeto.copy(empresaId = id))
        }
    }

    fun excluir(projeto: Projeto) {
        viewModelScope.launch { container.projetoRepository.excluir(projeto) }
    }
}
