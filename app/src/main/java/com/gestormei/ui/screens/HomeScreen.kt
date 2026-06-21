package com.gestormei.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gestormei.ui.components.AppCard
import com.gestormei.ui.components.EmptyState
import com.gestormei.ui.components.GraficoBarrasMensal
import com.gestormei.ui.components.GraficoGaugeLimite
import com.gestormei.ui.components.InfoRow
import com.gestormei.ui.components.SectionTitle
import com.gestormei.ui.theme.Amarelo
import com.gestormei.ui.theme.Laranja
import com.gestormei.ui.theme.Verde
import com.gestormei.ui.theme.Vermelho
import com.gestormei.util.Datas
import com.gestormei.util.Moeda
import com.gestormei.viewmodel.HomeUiState
import com.gestormei.viewmodel.HomeViewModel
import com.gestormei.viewmodel.NivelAlerta

@Composable
fun HomeScreen(
    onAbrirRelatorios: () -> Unit,
    onIrParaEmpresas: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (!state.temEmpresa) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EmptyState("Nenhuma empresa cadastrada ainda.")
            Spacer(Modifier.height(8.dp))
            Button(onClick = onIrParaEmpresas) { Text("Cadastrar empresa") }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = state.empresaNome,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        AlertaLimite(state)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Faturamento do mês", Moeda.formatar(state.faturamentoMes), Modifier.weight(1f))
            StatCard("Faturamento do ano", Moeda.formatar(state.faturamentoAno), Modifier.weight(1f))
        }

        if (state.metaMensal > 0) MetaCard(state)

        LimiteCard(state)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Compras do mês", Moeda.formatar(state.comprasMes), Modifier.weight(1f))
            StatCard("Compras do ano", Moeda.formatar(state.comprasAno), Modifier.weight(1f))
        }

        SectionTitle("Receitas x Despesas (ano atual)")
        AppCard {
            GraficoBarrasMensal(
                receitasPorMes = state.serieReceitas,
                despesasPorMes = state.serieDespesas
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Média mensal", Moeda.formatar(state.mediaMensal), Modifier.weight(1f))
            StatCard("Projeção dez.", Moeda.formatar(state.projecaoDezembro), Modifier.weight(1f))
        }

        StatCard(
            titulo = "DAS / Impostos pagos (ano)",
            valor = Moeda.formatar(state.impostosAno),
            modifier = Modifier.fillMaxWidth()
        )

        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DAS deste mês",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (state.dasPagoMes) "Pago" else "Pendente (vence dia 20)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (state.dasPagoMes) Verde else Laranja
                    )
                }
                Icon(
                    imageVector = if (state.dasPagoMes) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (state.dasPagoMes) Verde else Laranja
                )
            }
        }

        StatCard(
            titulo = "Projetos em andamento",
            valor = state.projetosEmAndamento.toString(),
            modifier = Modifier.fillMaxWidth()
        )

        SectionTitle("Próximas reuniões")
        AppCard {
            if (state.proximasReunioes.isEmpty()) {
                Text(
                    "Nenhum compromisso pendente.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                state.proximasReunioes.forEach { c ->
                    InfoRow(
                        label = "${Datas.paraBR(c.data)} ${c.hora}".trim(),
                        valor = c.titulo
                    )
                }
            }
        }

        Button(
            onClick = onAbrirRelatorios,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Assessment, contentDescription = null)
            Spacer(Modifier.height(0.dp))
            Text("  Ver relatórios")
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatCard(titulo: String, valor: String, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = valor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LimiteCard(state: HomeUiState) {
    val cor = corDoNivel(state.nivelAlerta)
    val percentual = (state.percentualUsado).coerceIn(0.0, 1.0).toFloat()
    AppCard {
        SectionTitle("Limite anual do MEI")
        GraficoGaugeLimite(percentual = percentual, cor = cor)
        Spacer(Modifier.height(8.dp))
        InfoRow("Limite anual", Moeda.formatar(state.limiteAnual))
        InfoRow("Faturado no ano", Moeda.formatar(state.faturamentoAno))
        InfoRow("Valor restante", Moeda.formatar(state.valorRestante))
        InfoRow("Percentual usado", "${(state.percentualUsado * 100).toInt()}%")
    }
}

@Composable
private fun MetaCard(state: HomeUiState) {
    val progresso = if (state.metaMensal > 0) {
        (state.faturamentoMes / state.metaMensal).coerceIn(0.0, 1.0).toFloat()
    } else 0f
    val atingiu = state.faturamentoMes >= state.metaMensal
    AppCard {
        SectionTitle("Meta do mês")
        InfoRow("Meta", Moeda.formatar(state.metaMensal))
        InfoRow("Faturado no mês", Moeda.formatar(state.faturamentoMes))
        InfoRow("Atingido", "${(progresso * 100).toInt()}%")
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progresso },
            modifier = Modifier.fillMaxWidth().height(10.dp),
            color = if (atingiu) Verde else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun AlertaLimite(state: HomeUiState) {
    if (state.nivelAlerta == NivelAlerta.NENHUM) return
    val cor = corDoNivel(state.nivelAlerta)
    val mensagem = when (state.nivelAlerta) {
        NivelAlerta.CRITICO -> "Atenção! Você atingiu 95% do limite anual do MEI."
        NivelAlerta.ALERTA -> "Você já usou 85% do limite anual do MEI."
        NivelAlerta.ATENCAO -> "Você já usou 70% do limite anual do MEI."
        NivelAlerta.NENHUM -> ""
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cor.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = cor)
            Text(text = mensagem, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun corDoNivel(nivel: NivelAlerta): Color = when (nivel) {
    NivelAlerta.NENHUM -> Verde
    NivelAlerta.ATENCAO -> Amarelo
    NivelAlerta.ALERTA -> Laranja
    NivelAlerta.CRITICO -> Vermelho
}
