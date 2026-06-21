package com.gestormei.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.gestormei.data.model.Ideia
import com.gestormei.ui.components.AppCard
import com.gestormei.ui.components.ConfirmDeleteDialog
import com.gestormei.ui.components.DropdownField
import com.gestormei.ui.components.EmptyState
import com.gestormei.ui.components.FormDialog
import com.gestormei.ui.components.FormTextField
import com.gestormei.ui.components.InfoRow
import com.gestormei.util.Datas
import com.gestormei.util.Opcoes
import com.gestormei.viewmodel.IdeiaViewModel

@Composable
fun IdeiasScreen(viewModel: IdeiaViewModel = viewModel()) {
    val ideias by viewModel.ideias.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<Ideia?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }
    var excluindo by remember { mutableStateOf<Ideia?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (ideias.isEmpty()) {
            EmptyState("Nenhuma ideia registrada.", Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ideias, key = { it.id }) { i ->
                    AppCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = i.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            AssistChip(onClick = {}, label = { Text(i.status) })
                        }
                        if (i.descricao.isNotBlank()) InfoRow("Descrição", i.descricao)
                        if (i.categoria.isNotBlank()) InfoRow("Categoria", i.categoria)
                        InfoRow("Prioridade", i.prioridade)
                        if (i.dataCriacao.isNotBlank()) InfoRow("Criada em", Datas.paraBR(i.dataCriacao))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            IconButton(onClick = { editando = i; mostrarForm = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { excluindo = i }) {
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
        ) { Icon(Icons.Default.Add, contentDescription = "Nova ideia") }
    }

    if (mostrarForm) {
        IdeiaForm(
            inicial = editando,
            onSalvar = { viewModel.salvar(it); mostrarForm = false },
            onDismiss = { mostrarForm = false }
        )
    }
    excluindo?.let { alvo ->
        ConfirmDeleteDialog(
            titulo = alvo.titulo,
            onConfirmar = { viewModel.excluir(alvo); excluindo = null },
            onCancelar = { excluindo = null }
        )
    }
}

@Composable
private fun IdeiaForm(
    inicial: Ideia?,
    onSalvar: (Ideia) -> Unit,
    onDismiss: () -> Unit
) {
    var titulo by remember { mutableStateOf(inicial?.titulo ?: "") }
    var descricao by remember { mutableStateOf(inicial?.descricao ?: "") }
    var categoria by remember { mutableStateOf(inicial?.categoria ?: "") }
    var prioridade by remember { mutableStateOf(inicial?.prioridade ?: "Média") }
    var status by remember { mutableStateOf(inicial?.status ?: Opcoes.statusIdeia.first()) }

    FormDialog(
        titulo = if (inicial == null) "Nova ideia" else "Editar ideia",
        salvarHabilitado = titulo.isNotBlank(),
        onSalvar = {
            onSalvar(
                (inicial ?: Ideia(empresaId = 0)).copy(
                    titulo = titulo.trim(),
                    descricao = descricao.trim(),
                    categoria = categoria.trim(),
                    prioridade = prioridade,
                    status = status
                )
            )
        },
        onDismiss = onDismiss
    ) {
        FormTextField("Título", titulo, { titulo = it })
        FormTextField("Descrição", descricao, { descricao = it }, singleLine = false, minLines = 2)
        FormTextField("Categoria", categoria, { categoria = it })
        DropdownField("Prioridade", Opcoes.prioridades, prioridade, { prioridade = it })
        DropdownField("Status", Opcoes.statusIdeia, status, { status = it })
    }
}
