package com.gestormei.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gestormei.GestorMeiApplication
import com.gestormei.data.model.EmailRegistro
import com.gestormei.data.model.Senha
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AcessoViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as GestorMeiApplication).container

    private val empresaId: StateFlow<Long?> = container.empresaAtivaId.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    val empresaAtivaId: StateFlow<Long?> = empresaId

    val senhas: StateFlow<List<Senha>> = empresaId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else container.acessoRepository.observarSenhas(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val emails: StateFlow<List<EmailRegistro>> = empresaId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else container.acessoRepository.observarEmails(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun salvarSenha(senha: Senha) {
        val id = empresaId.value ?: return
        viewModelScope.launch { container.acessoRepository.salvarSenha(senha.copy(empresaId = id)) }
    }

    fun excluirSenha(senha: Senha) {
        viewModelScope.launch { container.acessoRepository.excluirSenha(senha) }
    }

    fun salvarEmail(email: EmailRegistro) {
        val id = empresaId.value ?: return
        viewModelScope.launch { container.acessoRepository.salvarEmail(email.copy(empresaId = id)) }
    }

    fun excluirEmail(email: EmailRegistro) {
        viewModelScope.launch { container.acessoRepository.excluirEmail(email) }
    }
}
