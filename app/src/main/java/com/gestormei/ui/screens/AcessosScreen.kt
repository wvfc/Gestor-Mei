package com.gestormei.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gestormei.data.model.EmailRegistro
import com.gestormei.data.model.Senha
import com.gestormei.ui.components.AppCard
import com.gestormei.ui.components.ConfirmDeleteDialog
import com.gestormei.ui.components.EmptyState
import com.gestormei.ui.components.FormDialog
import com.gestormei.ui.components.FormTextField
import com.gestormei.ui.components.InfoRow
import com.gestormei.util.Acoes
import com.gestormei.util.BiometricAuth
import com.gestormei.viewmodel.AcessoViewModel

@Composable
fun AcessosScreen(viewModel: AcessoViewModel = viewModel()) {
    var aba by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val activity = context as? androidx.fragment.app.FragmentActivity
    val precisaBloqueio = remember { activity != null && BiometricAuth.disponivel(context) }
    var desbloqueado by remember { mutableStateOf(!precisaBloqueio) }
    var erro by remember { mutableStateOf<String?>(null) }

    fun desbloquear() {
        activity?.let {
            BiometricAuth.autenticar(
                it,
                onSucesso = { desbloqueado = true; erro = null },
                onErro = { msg -> erro = msg }
            )
        }
    }

    LaunchedEffect(precisaBloqueio) {
        if (precisaBloqueio) desbloquear()
    }

    if (!desbloqueado) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text("Cofre protegido", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            erro?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = { desbloquear() }) { Text("Desbloquear") }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = aba) {
            Tab(selected = aba == 0, onClick = { aba = 0 }, text = { Text("Senhas") })
            Tab(selected = aba == 1, onClick = { aba = 1 }, text = { Text("E-mails") })
        }
        if (aba == 0) SenhasTab(viewModel) else EmailsTab(viewModel)
    }
}

@Composable
private fun SenhasTab(viewModel: AcessoViewModel) {
    val senhas by viewModel.senhas.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<Senha?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }
    var excluindo by remember { mutableStateOf<Senha?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (senhas.isEmpty()) {
            EmptyState("Nenhum acesso salvo.", Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(senhas, key = { it.id }) { s ->
                    SenhaCard(
                        senha = s,
                        onEditar = { editando = s; mostrarForm = true },
                        onExcluir = { excluindo = s }
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { editando = null; mostrarForm = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "Novo acesso") }
    }

    if (mostrarForm) {
        SenhaForm(
            inicial = editando,
            onSalvar = { viewModel.salvarSenha(it); mostrarForm = false },
            onDismiss = { mostrarForm = false }
        )
    }
    excluindo?.let { alvo ->
        ConfirmDeleteDialog(
            titulo = alvo.nomeServico,
            onConfirmar = { viewModel.excluirSenha(alvo); excluindo = null },
            onCancelar = { excluindo = null }
        )
    }
}

@Composable
private fun SenhaCard(senha: Senha, onEditar: () -> Unit, onExcluir: () -> Unit) {
    var visivel by remember { mutableStateOf(false) }
    val context = LocalContext.current
    AppCard {
        Text(
            text = senha.nomeServico,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (senha.link.isNotBlank()) InfoRow("Link", senha.link)
        if (senha.login.isNotBlank()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                InfoRowExpandivel("Login", senha.login)
                IconButton(onClick = { Acoes.copiar(context, "Login", senha.login) }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar login")
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            InfoRowExpandivel("Senha", if (visivel) senha.senha else "••••••••")
            IconButton(onClick = { visivel = !visivel }) {
                Icon(
                    imageVector = if (visivel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (visivel) "Ocultar" else "Mostrar"
                )
            }
            IconButton(onClick = { Acoes.copiar(context, "Senha", senha.senha) }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar senha")
            }
        }
        if (senha.observacoes.isNotBlank()) InfoRow("Obs.", senha.observacoes)
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
            IconButton(onClick = onExcluir) { Icon(Icons.Default.Delete, contentDescription = "Excluir") }
        }
    }
}

@Composable
private fun InfoRowExpandivel(label: String, valor: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SenhaForm(
    inicial: Senha?,
    onSalvar: (Senha) -> Unit,
    onDismiss: () -> Unit
) {
    var nome by remember { mutableStateOf(inicial?.nomeServico ?: "") }
    var link by remember { mutableStateOf(inicial?.link ?: "") }
    var login by remember { mutableStateOf(inicial?.login ?: "") }
    var senha by remember { mutableStateOf(inicial?.senha ?: "") }
    var observacoes by remember { mutableStateOf(inicial?.observacoes ?: "") }
    var visivel by remember { mutableStateOf(false) }

    FormDialog(
        titulo = if (inicial == null) "Novo acesso" else "Editar acesso",
        salvarHabilitado = nome.isNotBlank(),
        onSalvar = {
            onSalvar(
                (inicial ?: Senha(empresaId = 0)).copy(
                    nomeServico = nome.trim(),
                    link = link.trim(),
                    login = login.trim(),
                    senha = senha,
                    observacoes = observacoes.trim()
                )
            )
        },
        onDismiss = onDismiss
    ) {
        FormTextField("Nome do site ou serviço", nome, { nome = it })
        FormTextField("Link", link, { link = it })
        FormTextField("Login ou e-mail usado", login, { login = it })
        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            singleLine = true,
            visualTransformation = if (visivel) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { visivel = !visivel }) {
                    Icon(
                        imageVector = if (visivel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        FormTextField("Observações", observacoes, { observacoes = it }, singleLine = false, minLines = 2)
    }
}

@Composable
private fun EmailsTab(viewModel: AcessoViewModel) {
    val emails by viewModel.emails.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<EmailRegistro?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }
    var excluindo by remember { mutableStateOf<EmailRegistro?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (emails.isEmpty()) {
            EmptyState("Nenhum e-mail cadastrado.", Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(emails, key = { it.id }) { e ->
                    AppCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = e.servico.ifBlank { e.email },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            AssistChip(onClick = {}, label = { Text("E-mail") })
                        }
                        InfoRow("E-mail", e.email)
                        if (e.finalidade.isNotBlank()) InfoRow("Finalidade", e.finalidade)
                        if (e.observacoes.isNotBlank()) InfoRow("Obs.", e.observacoes)
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            IconButton(onClick = { editando = e; mostrarForm = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { excluindo = e }) {
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
        ) { Icon(Icons.Default.Add, contentDescription = "Novo e-mail") }
    }

    if (mostrarForm) {
        EmailForm(
            inicial = editando,
            onSalvar = { viewModel.salvarEmail(it); mostrarForm = false },
            onDismiss = { mostrarForm = false }
        )
    }
    excluindo?.let { alvo ->
        ConfirmDeleteDialog(
            titulo = alvo.email,
            onConfirmar = { viewModel.excluirEmail(alvo); excluindo = null },
            onCancelar = { excluindo = null }
        )
    }
}

@Composable
private fun EmailForm(
    inicial: EmailRegistro?,
    onSalvar: (EmailRegistro) -> Unit,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf(inicial?.email ?: "") }
    var servico by remember { mutableStateOf(inicial?.servico ?: "") }
    var finalidade by remember { mutableStateOf(inicial?.finalidade ?: "") }
    var observacoes by remember { mutableStateOf(inicial?.observacoes ?: "") }

    FormDialog(
        titulo = if (inicial == null) "Novo e-mail" else "Editar e-mail",
        salvarHabilitado = email.isNotBlank(),
        onSalvar = {
            onSalvar(
                (inicial ?: EmailRegistro(empresaId = 0)).copy(
                    email = email.trim(),
                    servico = servico.trim(),
                    finalidade = finalidade.trim(),
                    observacoes = observacoes.trim()
                )
            )
        },
        onDismiss = onDismiss
    ) {
        FormTextField("E-mail", email, { email = it })
        FormTextField("Site ou serviço", servico, { servico = it })
        FormTextField("Finalidade", finalidade, { finalidade = it })
        FormTextField("Observações", observacoes, { observacoes = it }, singleLine = false, minLines = 2)
    }
}
