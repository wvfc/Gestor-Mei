package com.gestormei.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gestormei.data.model.Empresa
import com.gestormei.ui.components.ConfirmDeleteDialog
import com.gestormei.ui.components.FormTextField
import com.gestormei.ui.components.InfoRow
import com.gestormei.util.Acoes
import com.gestormei.util.Moeda
import com.gestormei.viewmodel.EmpresaViewModel

@Composable
fun EmpresasScreen(viewModel: EmpresaViewModel = viewModel()) {
    val empresas by viewModel.empresas.collectAsStateWithLifecycle()
    val selecionadaId by viewModel.empresaSelecionadaId.collectAsStateWithLifecycle()

    var editando by remember { mutableStateOf<Empresa?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }
    var excluindo by remember { mutableStateOf<Empresa?>(null) }

    val idAtivo = selecionadaId ?: empresas.firstOrNull()?.id

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(empresas, key = { it.id }) { empresa ->
                EmpresaCard(
                    empresa = empresa,
                    ativa = empresa.id == idAtivo,
                    onSelecionar = { viewModel.selecionar(empresa.id) },
                    onEditar = { editando = empresa; mostrarForm = true },
                    onExcluir = { excluindo = empresa }
                )
            }
        }

        FloatingActionButton(
            onClick = { editando = null; mostrarForm = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nova empresa")
        }
    }

    if (mostrarForm) {
        EmpresaFormDialog(
            inicial = editando,
            onSalvar = {
                viewModel.salvar(it)
                mostrarForm = false
            },
            onDismiss = { mostrarForm = false }
        )
    }

    excluindo?.let { alvo ->
        ConfirmDeleteDialog(
            titulo = alvo.nomeFantasia,
            onConfirmar = { viewModel.excluir(alvo); excluindo = null },
            onCancelar = { excluindo = null }
        )
    }
}

@Composable
private fun EmpresaCard(
    empresa: Empresa,
    ativa: Boolean,
    onSelecionar: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = empresa.nomeFantasia,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (ativa) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Ativa") },
                        leadingIcon = {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                        }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            if (empresa.razaoSocial.isNotBlank()) InfoRow("Razão social", empresa.razaoSocial)
            if (empresa.cnpj.isNotBlank()) InfoRow("CNPJ", empresa.cnpj)
            if (empresa.atividadePrincipal.isNotBlank()) InfoRow("Atividade", empresa.atividadePrincipal)
            if (empresa.telefone.isNotBlank()) InfoRow("Telefone", empresa.telefone)
            if (empresa.email.isNotBlank()) InfoRow("Email", empresa.email)
            if (empresa.chavePix.isNotBlank()) InfoRow("PIX", empresa.chavePix)
            InfoRow("Limite anual", Moeda.formatar(empresa.limiteAnual))
            if (empresa.observacoes.isNotBlank()) InfoRow("Obs.", empresa.observacoes)

            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (empresa.chavePix.isNotBlank()) {
                    IconButton(onClick = { Acoes.copiar(context, "PIX", empresa.chavePix) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar PIX")
                    }
                }
                if (empresa.telefone.isNotBlank()) {
                    IconButton(onClick = { Acoes.ligar(context, empresa.telefone) }) {
                        Icon(Icons.Default.Phone, contentDescription = "Ligar")
                    }
                }
                if (empresa.email.isNotBlank()) {
                    IconButton(onClick = { Acoes.email(context, empresa.email) }) {
                        Icon(Icons.Default.Email, contentDescription = "E-mail")
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!ativa) {
                    OutlinedButton(onClick = onSelecionar, modifier = Modifier.weight(1f)) {
                        Text("Selecionar")
                    }
                }
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onExcluir) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir")
                }
            }
        }
    }
}

@Composable
private fun EmpresaFormDialog(
    inicial: Empresa?,
    onSalvar: (Empresa) -> Unit,
    onDismiss: () -> Unit
) {
    var nomeFantasia by remember { mutableStateOf(inicial?.nomeFantasia ?: "") }
    var razaoSocial by remember { mutableStateOf(inicial?.razaoSocial ?: "") }
    var cnpj by remember { mutableStateOf(inicial?.cnpj ?: "") }
    var atividade by remember { mutableStateOf(inicial?.atividadePrincipal ?: "") }
    var telefone by remember { mutableStateOf(inicial?.telefone ?: "") }
    var email by remember { mutableStateOf(inicial?.email ?: "") }
    var pix by remember { mutableStateOf(inicial?.chavePix ?: "") }
    var observacoes by remember { mutableStateOf(inicial?.observacoes ?: "") }
    var limite by remember { mutableStateOf(((inicial?.limiteAnual ?: 81000.0)).toString()) }
    var meta by remember { mutableStateOf((inicial?.metaMensal ?: 0.0).takeIf { it > 0 }?.toString() ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (inicial == null) "Nova empresa" else "Editar empresa",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                FormTextField("Nome fantasia", nomeFantasia, { nomeFantasia = it })
                FormTextField("Razão social", razaoSocial, { razaoSocial = it })
                FormTextField("CNPJ", cnpj, { cnpj = it })
                FormTextField("Atividade principal", atividade, { atividade = it })
                FormTextField("Telefone", telefone, { telefone = it })
                FormTextField("Email principal", email, { email = it })
                FormTextField("Chave PIX", pix, { pix = it })
                FormTextField("Limite anual (R$)", limite, { limite = it })
                FormTextField("Meta de faturamento mensal (R$)", meta, { meta = it })
                FormTextField("Observações", observacoes, { observacoes = it }, singleLine = false, minLines = 2)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(
                        onClick = {
                            if (nomeFantasia.isBlank()) return@Button
                            onSalvar(
                                (inicial ?: Empresa(nomeFantasia = "")).copy(
                                    nomeFantasia = nomeFantasia.trim(),
                                    razaoSocial = razaoSocial.trim(),
                                    cnpj = cnpj.trim(),
                                    atividadePrincipal = atividade.trim(),
                                    telefone = telefone.trim(),
                                    email = email.trim(),
                                    chavePix = pix.trim(),
                                    observacoes = observacoes.trim(),
                                    limiteAnual = Moeda.parse(limite).takeIf { it > 0 } ?: 81000.0,
                                    metaMensal = Moeda.parse(meta)
                                )
                            )
                        }
                    ) { Text("Salvar") }
                }
            }
        }
    }
}
