package com.gestormei.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gestormei.GestorMeiApplication
import com.gestormei.data.model.Compromisso
import com.gestormei.data.model.Despesa
import com.gestormei.data.model.Empresa
import com.gestormei.data.model.Projeto
import com.gestormei.data.model.Receita
import com.gestormei.util.Datas
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class RelatorioUiState(
    val temEmpresa: Boolean = false,
    val empresaNome: String = "",
    val receitaMes: Double = 0.0,
    val receitaAno: Double = 0.0,
    val despesaMes: Double = 0.0,
    val despesaAno: Double = 0.0,
    val saldoMes: Double = 0.0,
    val saldoAno: Double = 0.0,
    val projetosAtivos: Int = 0,
    val compromissosPendentes: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class RelatorioViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as GestorMeiApplication).container

    val uiState = container.empresaAtivaId.flatMapLatest { id ->
        if (id == null) {
            flowOf(RelatorioUiState(temEmpresa = false))
        } else {
            combine(
                container.empresaRepository.observarPorId(id),
                container.financeiroRepository.observarReceitas(id),
                container.financeiroRepository.observarDespesas(id),
                container.projetoRepository.observarPorEmpresa(id),
                container.agendaRepository.observarPorEmpresa(id)
            ) { empresa, receitas, despesas, projetos, compromissos ->
                calcular(empresa, receitas, despesas, projetos, compromissos)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RelatorioUiState()
    )

    private fun calcular(
        empresa: Empresa?,
        receitas: List<Receita>,
        despesas: List<Despesa>,
        projetos: List<Projeto>,
        compromissos: List<Compromisso>
    ): RelatorioUiState {
        if (empresa == null) return RelatorioUiState(temEmpresa = false)
        val hoje = LocalDate.now()
        val ano = hoje.year
        val mes = hoje.monthValue

        val recAno = receitas.filter { Datas.parse(it.data)?.year == ano }
        val receitaAno = recAno.sumOf { it.valor }
        val receitaMes = recAno.filter { Datas.parse(it.data)?.monthValue == mes }.sumOf { it.valor }

        val despAno = despesas.filter { Datas.parse(it.data)?.year == ano }
        val despesaAno = despAno.sumOf { it.valor }
        val despesaMes = despAno.filter { Datas.parse(it.data)?.monthValue == mes }.sumOf { it.valor }

        val ativos = projetos.count { it.status != "Finalizado" && it.status != "Cancelado" }
        val pendentes = compromissos.count { it.status == "Pendente" }

        return RelatorioUiState(
            temEmpresa = true,
            empresaNome = empresa.nomeFantasia,
            receitaMes = receitaMes,
            receitaAno = receitaAno,
            despesaMes = despesaMes,
            despesaAno = despesaAno,
            saldoMes = receitaMes - despesaMes,
            saldoAno = receitaAno - despesaAno,
            projetosAtivos = ativos,
            compromissosPendentes = pendentes
        )
    }
}
