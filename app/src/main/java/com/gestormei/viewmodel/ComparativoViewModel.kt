package com.gestormei.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gestormei.GestorMeiApplication
import com.gestormei.data.model.Despesa
import com.gestormei.data.model.Empresa
import com.gestormei.data.model.Receita
import com.gestormei.util.Datas
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class ComparativoEmpresa(
    val nome: String,
    val faturamentoAno: Double,
    val despesaAno: Double,
    val saldoAno: Double,
    val impostosAno: Double,
    val limiteAnual: Double,
    val percentualLimite: Double
)

@OptIn(ExperimentalCoroutinesApi::class)
class ComparativoViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as GestorMeiApplication).container

    val empresas = container.empresaRepository.observarTodas().flatMapLatest { lista ->
        if (lista.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(
                lista.map { empresa ->
                    combine(
                        container.financeiroRepository.observarReceitas(empresa.id),
                        container.financeiroRepository.observarDespesas(empresa.id)
                    ) { receitas, despesas -> resumo(empresa, receitas, despesas) }
                }
            ) { it.toList() }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private fun resumo(empresa: Empresa, receitas: List<Receita>, despesas: List<Despesa>): ComparativoEmpresa {
        val ano = LocalDate.now().year
        val faturamento = receitas.filter { Datas.parse(it.data)?.year == ano }.sumOf { it.valor }
        val despAno = despesas.filter { Datas.parse(it.data)?.year == ano }
        val despesa = despAno.sumOf { it.valor }
        val impostos = despAno.filter { it.categoria == "Impostos" }.sumOf { it.valor }
        val limite = empresa.limiteAnual.takeIf { it > 0 } ?: 81000.0
        return ComparativoEmpresa(
            nome = empresa.nomeFantasia,
            faturamentoAno = faturamento,
            despesaAno = despesa,
            saldoAno = faturamento - despesa,
            impostosAno = impostos,
            limiteAnual = limite,
            percentualLimite = faturamento / limite
        )
    }
}
