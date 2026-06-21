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
import com.gestormei.data.model.Compromisso
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
import com.gestormei.viewmodel.AgendaViewModel

@Composable
fun AgendaScreen(viewModel: AgendaViewModel = viewModel()) {
    val compromissos by viewModel.compromissos.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<Compromisso?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }
    var excluindo by remember { mutableStateOf<Compromisso?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (compromissos.isEmpty()) {
            EmptyState("Nenhum compromisso cadastrado.", Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(compromissos, key = { it.id }) { c ->
                    AppCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = c.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            AssistChip(onClick = {}, label = { Text(c.status) })
                        }
                        InfoRow("Data", "${Datas.paraBR(c.data)} ${c.hora}".trim())
                        if (c.cliente.isNotBlank()) InfoRow("Cliente", c.cliente)
                        if (c.descricao.isNotBlank()) InfoRow("Descrição", c.descricao)
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            IconButton(onClick = { editando = c; mostrarForm = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { excluindo = c }) {
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
        ) { Icon(Icons.Default.Add, contentDescription = "Novo compromisso") }
    }

    if (mostrarForm) {
        CompromissoForm(
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
private fun CompromissoForm(
    inicial: Compromisso?,
    onSalvar: (Compromisso) -> Unit,
    onDismiss: () -> Unit
) {
    var titulo by remember { mutableStateOf(inicial?.titulo ?: "") }
    var data by remember { mutableStateOf(inicial?.data ?: Datas.hoje()) }
    var hora by remember { mutableStateOf(inicial?.hora ?: "") }
    var cliente by remember { mutableStateOf(inicial?.cliente ?: "") }
    var descricao by remember { mutableStateOf(inicial?.descricao ?: "") }
    var status by remember { mutableStateOf(inicial?.status ?: Opcoes.statusCompromisso.first()) }

    FormDialog(
        titulo = if (inicial == null) "Novo compromisso" else "Editar compromisso",
        salvarHabilitado = titulo.isNotBlank(),
        onSalvar = {
            onSalvar(
                (inicial ?: Compromisso(empresaId = 0)).copy(
                    titulo = titulo.trim(),
                    data = data,
                    hora = hora.trim(),
                    cliente = cliente.trim(),
                    descricao = descricao.trim(),
                    status = status
                )
            )
        },
        onDismiss = onDismiss
    ) {
        FormTextField("Título", titulo, { titulo = it })
        DatePickerField("Data", data, { data = it })
        FormTextField("Hora (ex: 14:30)", hora, { hora = it })
        FormTextField("Cliente / empresa relacionada", cliente, { cliente = it })
        FormTextField("Descrição", descricao, { descricao = it }, singleLine = false, minLines = 2)
        DropdownField("Status", Opcoes.statusCompromisso, status, { status = it })
    }
}
