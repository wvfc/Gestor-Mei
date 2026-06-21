package com.gestormei.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Guarda qual empresa está selecionada no momento, de forma persistente.
 * Compartilhado entre todas as telas do app.
 */
class SelectionManager(context: Context) {

    private val prefs = context.getSharedPreferences("gestor_mei_selection", Context.MODE_PRIVATE)

    private val _selectedId = MutableStateFlow(
        prefs.getLong(KEY_EMPRESA, -1L).takeIf { it > 0 }
    )
    val selectedId: StateFlow<Long?> = _selectedId.asStateFlow()

    fun selecionar(id: Long) {
        _selectedId.value = id
        prefs.edit().putLong(KEY_EMPRESA, id).apply()
    }

    companion object {
        private const val KEY_EMPRESA = "empresa_selecionada"
    }
}
