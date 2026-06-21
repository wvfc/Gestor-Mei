package com.gestormei.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gestormei.GestorMeiApplication
import com.gestormei.data.model.Cliente
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ClienteViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as GestorMeiApplication).container

    private val empresaId: StateFlow<Long?> = container.empresaAtivaId.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    val clientes: StateFlow<List<Cliente>> = empresaId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else container.clienteRepository.observarPorEmpresa(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun salvar(cliente: Cliente) {
        val id = empresaId.value ?: return
        viewModelScope.launch { container.clienteRepository.salvar(cliente.copy(empresaId = id)) }
    }

    fun excluir(cliente: Cliente) {
        viewModelScope.launch { container.clienteRepository.excluir(cliente) }
    }

    fun excluirTodos() {
        val id = empresaId.value ?: return
        viewModelScope.launch { container.clienteRepository.excluirTodos(id) }
    }
}
