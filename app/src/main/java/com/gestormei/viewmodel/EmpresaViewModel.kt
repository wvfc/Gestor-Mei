package com.gestormei.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gestormei.GestorMeiApplication
import com.gestormei.data.model.Empresa
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EmpresaViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as GestorMeiApplication).container

    val empresas: StateFlow<List<Empresa>> =
        container.empresaRepository.observarTodas().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val empresaSelecionadaId: StateFlow<Long?> = container.selectionManager.selectedId

    fun selecionar(id: Long) = container.selectionManager.selecionar(id)

    fun salvar(empresa: Empresa) {
        viewModelScope.launch {
            if (empresa.id == 0L) {
                val novoId = container.empresaRepository.inserir(empresa)
                container.selectionManager.selecionar(novoId)
            } else {
                container.empresaRepository.atualizar(empresa)
            }
        }
    }

    fun excluir(empresa: Empresa) {
        viewModelScope.launch {
            container.empresaRepository.excluir(empresa)
        }
    }
}
