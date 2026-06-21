package com.gestormei.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gestormei.GestorMeiApplication
import com.gestormei.data.model.Ideia
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class IdeiaViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as GestorMeiApplication).container

    private val empresaId: StateFlow<Long?> = container.empresaAtivaId.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    val empresaAtivaId: StateFlow<Long?> = empresaId

    val ideias: StateFlow<List<Ideia>> = empresaId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else container.ideiaRepository.observarPorEmpresa(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun salvar(ideia: Ideia) {
        val id = empresaId.value ?: return
        viewModelScope.launch {
            val comData = if (ideia.id == 0L && ideia.dataCriacao.isBlank()) {
                ideia.copy(dataCriacao = com.gestormei.util.Datas.hoje())
            } else ideia
            container.ideiaRepository.salvar(comData.copy(empresaId = id))
        }
    }

    fun excluir(ideia: Ideia) {
        viewModelScope.launch { container.ideiaRepository.excluir(ideia) }
    }
}
