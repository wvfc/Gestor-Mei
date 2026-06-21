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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gestormei.ui.components.AppCard
import com.gestormei.ui.components.EmptyState
import com.gestormei.ui.components.InfoRow
import com.gestormei.ui.components.SectionTitle
import com.gestormei.util.Moeda
import com.gestormei.viewmodel.RelatorioViewModel

@Composable
fun RelatoriosScreen(
    onVoltar: () -> Unit,
    viewModel: RelatorioViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (!state.temEmpresa) {
        EmptyState("Cadastre uma empresa para ver os relatórios.")
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
            text = "Relatório — ${state.empresaNome}",
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

        Text(
            text = "Exportar em PDF estará disponível em uma versão futura.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedButton(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar ao início")
        }
        Spacer(Modifier.height(8.dp))
    }
}
