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

enum class NivelAlerta { NENHUM, ATENCAO, ALERTA, CRITICO }

data class HomeUiState(
    val temEmpresa: Boolean = false,
    val empresaNome: String = "",
    val faturamentoMes: Double = 0.0,
    val faturamentoAno: Double = 0.0,
    val limiteAnual: Double = 81000.0,
    val valorRestante: Double = 81000.0,
    val percentualUsado: Double = 0.0,
    val comprasMes: Double = 0.0,
    val comprasAno: Double = 0.0,
    val mediaMensal: Double = 0.0,
    val projecaoDezembro: Double = 0.0,
    val proximasReunioes: List<Compromisso> = emptyList(),
    val projetosEmAndamento: Int = 0,
    val nivelAlerta: NivelAlerta = NivelAlerta.NENHUM,
    val serieReceitas: List<Double> = List(12) { 0.0 },
    val serieDespesas: List<Double> = List(12) { 0.0 },
    val metaMensal: Double = 0.0,
    val impostosAno: Double = 0.0,
    val dasPagoMes: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as GestorMeiApplication).container

    val uiState = container.empresaAtivaId.flatMapLatest { id ->
        if (id == null) {
            flowOf(HomeUiState(temEmpresa = false))
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
        initialValue = HomeUiState()
    )

    private fun calcular(
        empresa: Empresa?,
        receitas: List<Receita>,
        despesas: List<Despesa>,
        projetos: List<Projeto>,
        compromissos: List<Compromisso>
    ): HomeUiState {
        if (empresa == null) return HomeUiState(temEmpresa = false)

        val hoje = LocalDate.now()
        val ano = hoje.year
        val mes = hoje.monthValue

        val recAno = receitas.filter { Datas.parse(it.data)?.year == ano }
        // faturamento (limite do MEI) considera apenas receitas de venda
        val recFat = recAno.filter { it.contaNoLimite }
        val faturamentoAno = recFat.sumOf { it.valor }
        val faturamentoMes = recFat
            .filter { Datas.parse(it.data)?.monthValue == mes }
            .sumOf { it.valor }

        val despAno = despesas.filter { Datas.parse(it.data)?.year == ano }
        val comprasAno = despAno.sumOf { it.valor }
        val comprasMes = despAno
            .filter { Datas.parse(it.data)?.monthValue == mes }
            .sumOf { it.valor }
        val impostosAno = despAno.filter { it.categoria == "Impostos" }.sumOf { it.valor }
        val dasPagoMes = despAno.any {
            it.categoria == "Impostos" && Datas.parse(it.data)?.monthValue == mes
        }

        val limite = empresa.limiteAnual.takeIf { it > 0 } ?: 81000.0
        val percentual = faturamentoAno / limite
        val mediaMensal = faturamentoAno / mes
        val projecao = mediaMensal * 12

        val proximas = compromissos
            .filter { it.status == "Pendente" }
            .filter { (Datas.parse(it.data) ?: hoje) >= hoje }
            .sortedBy { it.data }
            .take(3)

        val emAndamento = projetos.count { it.status == "Em andamento" }

        val serieRec = DoubleArray(12)
        recAno.forEach { r -> Datas.parse(r.data)?.let { serieRec[it.monthValue - 1] += r.valor } }
        val serieDesp = DoubleArray(12)
        despAno.forEach { d -> Datas.parse(d.data)?.let { serieDesp[it.monthValue - 1] += d.valor } }

        val nivel = when {
            percentual >= 0.95 -> NivelAlerta.CRITICO
            percentual >= 0.85 -> NivelAlerta.ALERTA
            percentual >= 0.70 -> NivelAlerta.ATENCAO
            else -> NivelAlerta.NENHUM
        }

        return HomeUiState(
            temEmpresa = true,
            empresaNome = empresa.nomeFantasia,
            faturamentoMes = faturamentoMes,
            faturamentoAno = faturamentoAno,
            limiteAnual = limite,
            valorRestante = (limite - faturamentoAno).coerceAtLeast(0.0),
            percentualUsado = percentual,
            comprasMes = comprasMes,
            comprasAno = comprasAno,
            mediaMensal = mediaMensal,
            projecaoDezembro = projecao,
            proximasReunioes = proximas,
            projetosEmAndamento = emAndamento,
            nivelAlerta = nivel,
            serieReceitas = serieRec.toList(),
            serieDespesas = serieDesp.toList(),
            metaMensal = empresa.metaMensal,
            impostosAno = impostosAno,
            dasPagoMes = dasPagoMes
        )
    }
}
