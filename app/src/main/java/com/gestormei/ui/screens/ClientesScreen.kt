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
import com.gestormei.data.model.Cliente
import com.gestormei.ui.components.AppCard
import com.gestormei.ui.components.ConfirmDeleteDialog
import com.gestormei.ui.components.EmptyState
import com.gestormei.ui.components.FormDialog
import com.gestormei.ui.components.FormTextField
import com.gestormei.ui.components.InfoRow
import com.gestormei.viewmodel.ClienteViewModel

@Composable
fun ClientesScreen(viewModel: ClienteViewModel = viewModel()) {
    val clientes by viewModel.clientes.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<Cliente?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }
    var excluindo by remember { mutableStateOf<Cliente?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (clientes.isEmpty()) {
            EmptyState(
                "Nenhum cliente cadastrado.\nUse Configurações para importar de um CSV.",
                Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(clientes, key = { it.id }) { c ->
                    AppCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = c.nome,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { editando = c; mostrarForm = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { excluindo = c }) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir")
                            }
                        }
                        if (c.email.isNotBlank()) InfoRow("E-mail", c.email)
                        if (c.telefone.isNotBlank()) InfoRow("Telefone", c.telefone)
                        if (c.observacoes.isNotBlank()) InfoRow("Obs.", c.observacoes)
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { editando = null; mostrarForm = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "Novo cliente") }
    }

    if (mostrarForm) {
        ClienteForm(
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
private fun ClienteForm(
    inicial: Cliente?,
    onSalvar: (Cliente) -> Unit,
    onDismiss: () -> Unit
) {
    var nome by remember { mutableStateOf(inicial?.nome ?: "") }
    var email by remember { mutableStateOf(inicial?.email ?: "") }
    var telefone by remember { mutableStateOf(inicial?.telefone ?: "") }
    var observacoes by remember { mutableStateOf(inicial?.observacoes ?: "") }

    FormDialog(
        titulo = if (inicial == null) "Novo cliente" else "Editar cliente",
        salvarHabilitado = nome.isNotBlank(),
        onSalvar = {
            onSalvar(
                (inicial ?: Cliente(empresaId = 0)).copy(
                    nome = nome.trim(),
                    email = email.trim(),
                    telefone = telefone.trim(),
                    observacoes = observacoes.trim()
                )
            )
        },
        onDismiss = onDismiss
    ) {
        FormTextField("Nome", nome, { nome = it })
        FormTextField("E-mail", email, { email = it })
        FormTextField("Telefone", telefone, { telefone = it })
        FormTextField("Observações", observacoes, { observacoes = it }, singleLine = false, minLines = 2)
    }
}
