package com.gestormei.data

import android.content.Context
import com.gestormei.data.database.AppDatabase
import com.gestormei.data.repository.AcessoRepository
import com.gestormei.data.repository.AgendaRepository
import com.gestormei.data.repository.ClienteRepository
import com.gestormei.data.repository.EmpresaRepository
import com.gestormei.data.repository.FinanceiroRepository
import com.gestormei.data.repository.IdeiaRepository
import com.gestormei.data.repository.ProjetoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Injeção de dependências manual (Service Locator simples).
 * Mantém uma única instância do banco e dos repositórios.
 */
class AppContainer(context: Context) {

    private val db = AppDatabase.getInstance(context)

    val selectionManager = SelectionManager(context)
    val configManager = ConfigManager(context)
    val backupManager = BackupManager(db)

    val empresaRepository = EmpresaRepository(db.empresaDao())
    val financeiroRepository = FinanceiroRepository(db.receitaDao(), db.despesaDao())
    val acessoRepository = AcessoRepository(db.senhaDao(), db.emailDao())
    val agendaRepository = AgendaRepository(db.compromissoDao())
    val projetoRepository = ProjetoRepository(db.projetoDao())
    val ideiaRepository = IdeiaRepository(db.ideiaDao())
    val clienteRepository = ClienteRepository(db.clienteDao())

    /**
     * Id da empresa efetivamente ativa: a selecionada, ou a primeira da lista
     * caso a seleção esteja vazia/inválida. Emite null quando não há empresas.
     */
    val empresaAtivaId: Flow<Long?> =
        combine(selectionManager.selectedId, empresaRepository.observarTodas()) { selecionada, lista ->
            when {
                lista.isEmpty() -> null
                selecionada != null && lista.any { it.id == selecionada } -> selecionada
                else -> lista.first().id
            }
        }
}
