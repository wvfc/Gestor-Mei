package com.gestormei.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gestormei.data.model.Projeto
import com.gestormei.ui.components.AppCard
import com.gestormei.ui.components.ConfirmDeleteDialog
import com.gestormei.ui.components.DatePickerField
import com.gestormei.ui.components.DropdownField
import com.gestormei.ui.components.EmptyState
import com.gestormei.ui.components.FormDialog
import com.gestormei.ui.components.FormTextField
import com.gestormei.ui.components.InfoRow
import com.gestormei.util.Datas
import com.gestormei.util.Opcoes
import com.gestormei.viewmodel.ProjetoViewModel

@Composable
fun ProjetosScreen(viewModel: ProjetoViewModel = viewModel()) {
    val projetos by viewModel.projetos.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<Projeto?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }
    var excluindo by remember { mutableStateOf<Projeto?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (projetos.isEmpty()) {
            EmptyState("Nenhum projeto cadastrado.", Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(projetos, key = { it.id }) { p ->
                    AppCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = p.nome,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            AssistChip(onClick = {}, label = { Text(p.status) })
                        }
                        if (p.cliente.isNotBlank()) InfoRow("Cliente", p.cliente)
                        if (p.descricao.isNotBlank()) InfoRow("Descrição", p.descricao)
                        if (p.dataInicio.isNotBlank()) InfoRow("Início", Datas.paraBR(p.dataInicio))
                        if (p.dataEntrega.isNotBlank()) InfoRow("Entrega", Datas.paraBR(p.dataEntrega))
                        InfoRow("Prioridade", p.prioridade)
                        if (p.observacoes.isNotBlank()) InfoRow("Obs.", p.observacoes)
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            IconButton(onClick = { editando = p; mostrarForm = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { excluindo = p }) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir")
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { editando = null; mostrarForm = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "Novo projeto") }
    }

    if (mostrarForm) {
        ProjetoForm(
            inicial = editando,
            onSalvar = { viewModel.salvar(it); mostrarForm = false },
            onDismiss = { mostrarForm = false }
        )
    }
    excluindo?.let { alvo ->
        ConfirmDeleteDialog(
            titulo = alvo.nome,
            onConfirmar = { viewModel.excluir(alvo); excluindo = null },
            onCancelar = { excluindo = null }
        )
    }
}

@Composable
private fun ProjetoForm(
    inicial: Projeto?,
    onSalvar: (Projeto) -> Unit,
    onDismiss: () -> Unit
) {
    var nome by remember { mutableStateOf(inicial?.nome ?: "") }
    var cliente by remember { mutableStateOf(inicial?.cliente ?: "") }
    var descricao by remember { mutableStateOf(inicial?.descricao ?: "") }
    var inicio by remember { mutableStateOf(inicial?.dataInicio ?: Datas.hoje()) }
    var entrega by remember { mutableStateOf(inicial?.dataEntrega ?: "") }
    var status by remember { mutableStateOf(inicial?.status ?: Opcoes.statusProjeto.first()) }
    var prioridade by remember { mutableStateOf(inicial?.prioridade ?: "Média") }
    var observacoes by remember { mutableStateOf(inicial?.observacoes ?: "") }

    FormDialog(
        titulo = if (inicial == null) "Novo projeto" else "Editar projeto",
        salvarHabilitado = nome.isNotBlank(),
        onSalvar = {
            onSalvar(
                (inicial ?: Projeto(empresaId = 0)).copy(
                    nome = nome.trim(),
                    cliente = cliente.trim(),
                    descricao = descricao.trim(),
                    dataInicio = inicio,
                    dataEntrega = entrega,
                    status = status,
                    prioridade = prioridade,
                    observacoes = observacoes.trim()
                )
            )
        },
        onDismiss = onDismiss
    ) {
        FormTextField("Nome do projeto", nome, { nome = it })
        FormTextField("Cliente", cliente, { cliente = it })
        FormTextField("Descrição", descricao, { descricao = it }, singleLine = false, minLines = 2)
        DatePickerField("Data de início", inicio, { inicio = it })
        DatePickerField("Data prevista de entrega", entrega, { entrega = it })
        DropdownField("Status", Opcoes.statusProjeto, status, { status = it })
        DropdownField("Prioridade", Opcoes.prioridades, prioridade, { prioridade = it })
        FormTextField("Observações", observacoes, { observacoes = it }, singleLine = false, minLines = 2)
    }
}
