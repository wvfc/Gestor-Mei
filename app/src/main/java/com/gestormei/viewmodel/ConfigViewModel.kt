package com.gestormei.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gestormei.GestorMeiApplication
import com.gestormei.data.ConfigManager
import com.gestormei.util.Importacao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConfigViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as GestorMeiApplication).container
    private val config: ConfigManager = container.configManager

    var openAiKey by mutableConfig(config.openAiKey) { config.openAiKey = it }
    var openAiModel by mutableConfig(config.openAiModel) { config.openAiModel = it }
    var iaAnexosAtiva by mutableConfig(config.iaAnexosAtiva) { config.iaAnexosAtiva = it }

    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem: StateFlow<String?> = _mensagem.asStateFlow()

    fun limparMensagem() { _mensagem.value = null }

    fun salvarChave(valor: String) { openAiKey = valor }
    fun selecionarModelo(valor: String) { openAiModel = valor }
    fun alternarIaAnexos(valor: Boolean) { iaAnexosAtiva = valor }

    fun importarClientes(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val empresaId = container.empresaAtivaId.first()
                    ?: error("Selecione uma empresa antes de importar.")
                withContext(Dispatchers.IO) {
                    val linhas = Importacao.lerLinhas(getApplication(), uri)
                    val resultado = Importacao.clientesDeCsv(linhas, empresaId)
                    if (resultado.itens.isNotEmpty()) {
                        container.clienteRepository.inserirVarios(resultado.itens)
                    }
                    resultado
                }
            }.onSuccess { r ->
                _mensagem.value = "Importados ${r.itens.size} clientes" +
                    if (r.ignorados > 0) " (${r.ignorados} linhas ignoradas)." else "."
            }.onFailure {
                _mensagem.value = "Falha ao importar clientes: ${it.message}"
            }
        }
    }

    fun importarDespesasNubank(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val empresaId = container.empresaAtivaId.first()
                    ?: error("Selecione uma empresa antes de importar.")
                withContext(Dispatchers.IO) {
                    val linhas = Importacao.lerLinhas(getApplication(), uri)
                    val resultado = Importacao.despesasNubankDeCsv(linhas, empresaId)
                    resultado.itens.forEach { container.financeiroRepository.salvarDespesa(it) }
                    resultado
                }
            }.onSuccess { r ->
                _mensagem.value = "Importadas ${r.itens.size} despesas do Nubank" +
                    if (r.ignorados > 0) " (${r.ignorados} linhas ignoradas)." else "."
            }.onFailure {
                _mensagem.value = "Falha ao importar despesas: ${it.message}"
            }
        }
    }
}

/** Pequeno delegate para expor um valor de configuração como estado mutável simples. */
private fun <T> mutableConfig(inicial: T, salvar: (T) -> Unit) =
    object : kotlin.properties.ReadWriteProperty<Any?, T> {
        private val state = androidx.compose.runtime.mutableStateOf(inicial)
        override fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): T = state.value
        override fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: T) {
            state.value = value
            salvar(value)
        }
    }
