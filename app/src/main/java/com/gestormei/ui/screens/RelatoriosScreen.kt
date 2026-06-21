package com.gestormei.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gestormei.ui.components.AppCard
import com.gestormei.ui.components.DropdownField
import com.gestormei.ui.components.EmptyState
import com.gestormei.ui.components.FatiaCategoria
import com.gestormei.ui.components.GraficoBarrasMensal
import com.gestormei.ui.components.GraficoDonutCategorias
import com.gestormei.ui.components.InfoRow
import com.gestormei.ui.components.SectionTitle
import com.gestormei.ui.components.paletaGraficos
import com.gestormei.util.Datas
import com.gestormei.util.Exportacao
import com.gestormei.util.Moeda
import com.gestormei.viewmodel.ComparativoViewModel
import com.gestormei.viewmodel.RelatorioViewModel
import java.time.LocalDate

@Composable
fun RelatoriosScreen(
    onVoltar: () -> Unit,
    viewModel: RelatorioViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var aba by remember { mutableIntStateOf(0) }

    if (!state.temEmpresa) {
        EmptyState("Cadastre uma empresa para ver os relatórios.")
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = aba) {
            Tab(selected = aba == 0, onClick = { aba = 0 }, text = { Text("Resumo") })
            Tab(selected = aba == 1, onClick = { aba = 1 }, text = { Text("Gráficos") })
            Tab(selected = aba == 2, onClick = { aba = 2 }, text = { Text("Empresas") })
        }
        when (aba) {
            0 -> ResumoTab(state.empresaNome, viewModel, onVoltar)
            1 -> GraficosTab(viewModel)
            else -> ComparativoTab()
        }
    }
}

@Composable
private fun ComparativoTab(viewModel: ComparativoViewModel = viewModel()) {
    val empresas by viewModel.empresas.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Comparativo entre empresas (ano atual)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (empresas.size < 2) {
            Text(
                "Cadastre duas empresas para comparar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        empresas.forEach { e ->
            AppCard {
                Text(
                    text = e.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                InfoRow("Faturamento", Moeda.formatar(e.faturamentoAno))
                InfoRow("Despesas", Moeda.formatar(e.despesaAno))
                InfoRow("Saldo", Moeda.formatar(e.saldoAno))
                InfoRow("DAS / Impostos", Moeda.formatar(e.impostosAno))
                InfoRow("Limite usado", "${(e.percentualLimite * 100).toInt()}% de ${Moeda.formatar(e.limiteAnual)}")
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ResumoTab(
    empresaNome: String,
    viewModel: RelatorioViewModel,
    onVoltar: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Relatório — $empresaNome",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        AppCard {
            SectionTitle("Receitas")
            InfoRow("Receita do mês", Moeda.formatar(state.receitaMes))
            InfoRow("Receita do ano", Moeda.formatar(state.receitaAno))
        }
        AppCard {
            SectionTitle("Despesas / Compras")
            InfoRow("Despesas do mês", Moeda.formatar(state.despesaMes))
            InfoRow("Despesas do ano", Moeda.formatar(state.despesaAno))
        }
        AppCard {
            SectionTitle("Saldo estimado")
            InfoRow("Saldo do mês", Moeda.formatar(state.saldoMes))
            InfoRow("Saldo do ano", Moeda.formatar(state.saldoAno))
        }
        AppCard {
            SectionTitle("Atividades")
            InfoRow("Projetos ativos", state.projetosAtivos.toString())
            InfoRow("Compromissos pendentes", state.compromissosPendentes.toString())
        }

        Button(
            onClick = {
                Exportacao.compartilharRelatorioPdf(
                    context = context,
                    titulo = "Relatório — $empresaNome",
                    linhas = listOf(
                        "Receita do mês" to Moeda.formatar(state.receitaMes),
                        "Receita do ano" to Moeda.formatar(state.receitaAno),
                        "" to "",
                        "Despesas do mês" to Moeda.formatar(state.despesaMes),
                        "Despesas do ano" to Moeda.formatar(state.despesaAno),
                        "" to "",
                        "Saldo do mês" to Moeda.formatar(state.saldoMes),
                        "Saldo do ano" to Moeda.formatar(state.saldoAno),
                        "" to "",
                        "Projetos ativos" to state.projetosAtivos.toString(),
                        "Compromissos pendentes" to state.compromissosPendentes.toString()
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Exportar relatório em PDF")
        }
        OutlinedButton(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar ao início")
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun GraficosTab(viewModel: RelatorioViewModel) {
    val receitas by viewModel.receitas.collectAsStateWithLifecycle()
    val despesas by viewModel.despesas.collectAsStateWithLifecycle()

    val anosDisponiveis = remember(receitas, despesas) {
        val anoAtual = LocalDate.now().year
        val conjunto = (receitas.map { it.data } + despesas.map { it.data })
            .mapNotNull { Datas.parse(it)?.year }
            .toMutableSet()
        conjunto.add(anoAtual)
        conjunto.sortedDescending().map { it.toString() }
    }
    var ano by remember(anosDisponiveis) {
        mutableStateOf(anosDisponiveis.firstOrNull() ?: LocalDate.now().year.toString())
    }
    val anoInt = ano.toIntOrNull() ?: LocalDate.now().year

    val serieReceitas = remember(receitas, anoInt) { serieMensal(receitas.map { it.data to it.valor }, anoInt) }
    val serieDespesas = remember(despesas, anoInt) { serieMensal(despesas.map { it.data to it.valor }, anoInt) }

    val fatiasDespesa = remember(despesas, anoInt) {
        despesas
            .filter { Datas.parse(it.data)?.year == anoInt }
            .groupBy { it.categoria.ifBlank { "Outros" } }
            .map { (cat, lista) -> cat to lista.sumOf { it.valor } }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .mapIndexed { i, (cat, valor) ->
                FatiaCategoria(cat, valor, paletaGraficos[i % paletaGraficos.size])
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DropdownField(
            label = "Ano",
            options = anosDisponiveis,
            selected = ano,
            onSelected = { ano = it }
        )

        AppCard {
            SectionTitle("Receitas x Despesas por mês")
            GraficoBarrasMensal(serieReceitas, serieDespesas)
        }

        AppCard {
            SectionTitle("Despesas por categoria")
            if (fatiasDespesa.isEmpty()) {
                Text(
                    "Sem despesas registradas em $ano.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                GraficoDonutCategorias(fatiasDespesa)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

private fun serieMensal(itens: List<Pair<String, Double>>, ano: Int): List<Double> {
    val arr = DoubleArray(12)
    itens.forEach { (data, valor) ->
        Datas.parse(data)?.let { if (it.year == ano) arr[it.monthValue - 1] += valor }
    }
    return arr.toList()
}
