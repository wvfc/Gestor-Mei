package com.gestormei.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gestormei.GestorMeiApplication
import com.gestormei.data.ai.OpenAiService
import com.gestormei.data.model.Despesa
import com.gestormei.data.model.Receita
import com.gestormei.ui.components.AppCard
import com.gestormei.ui.components.ConfirmDeleteDialog
import com.gestormei.ui.components.DatePickerField
import com.gestormei.ui.components.DropdownField
import com.gestormei.ui.components.EmptyState
import com.gestormei.ui.components.FormDialog
import com.gestormei.ui.components.FormTextField
import com.gestormei.ui.components.InfoRow
import com.gestormei.ui.components.SearchField
import com.gestormei.util.Datas
import com.gestormei.util.Exportacao
import com.gestormei.util.Moeda
import com.gestormei.util.Opcoes
import com.gestormei.viewmodel.FinanceiroViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun FinanceiroScreen(viewModel: FinanceiroViewModel = viewModel()) {
    var aba by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = aba) {
            Tab(selected = aba == 0, onClick = { aba = 0 }, text = { Text("Receitas") })
            Tab(selected = aba == 1, onClick = { aba = 1 }, text = { Text("Despesas") })
        }
        if (aba == 0) ReceitasTab(viewModel) else DespesasTab(viewModel)
    }
}

@Composable
private fun FiltroPeriodo(
    mes: Int,
    ano: String,
    anosDisponiveis: List<String>,
    onMes: (Int) -> Unit,
    onAno: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DropdownField(
            label = "Mês",
            options = Opcoes.meses,
            selected = Opcoes.meses[mes],
            onSelected = { onMes(Opcoes.meses.indexOf(it)) },
            modifier = Modifier.weight(1f)
        )
        DropdownField(
            label = "Ano",
            options = anosDisponiveis,
            selected = ano,
            onSelected = onAno,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun anos(datas: List<String>): List<String> {
    val anoAtual = LocalDate.now().year
    val conjunto = datas.mapNotNull { Datas.parse(it)?.year }.toMutableSet()
    conjunto.add(anoAtual)
    return listOf("Todos") + conjunto.sortedDescending().map { it.toString() }
}

@Composable
private fun TotalComExport(total: String, cor: Color, onExport: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Total: $total",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = cor
        )
        TextButton(onClick = onExport) {
            Icon(Icons.Default.Share, contentDescription = null)
            Text("  CSV")
        }
    }
}

private fun combinaPeriodo(data: String, mes: Int, ano: String): Boolean {
    val d = Datas.parse(data) ?: return mes == 0 && ano == "Todos"
    val okMes = mes == 0 || d.monthValue == mes
    val okAno = ano == "Todos" || d.year.toString() == ano
    return okMes && okAno
}

@Composable
private fun ReceitasTab(viewModel: FinanceiroViewModel) {
    val receitas by viewModel.receitas.collectAsStateWithLifecycle()
    var mes by remember { mutableIntStateOf(0) }
    var ano by remember { mutableStateOf("Todos") }
    var editando by remember { mutableStateOf<Receita?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }
    var excluindo by remember { mutableStateOf<Receita?>(null) }
    var busca by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filtradas = remember(receitas, mes, ano, busca) {
        receitas.filter { combinaPeriodo(it.data, mes, ano) }
            .filter {
                busca.isBlank() || it.cliente.contains(busca, true) ||
                    it.descricao.contains(busca, true) || it.formaPagamento.contains(busca, true)
            }
    }
    val total = filtradas.sumOf { it.valor }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(12.dp))
            FiltroPeriodo(mes, ano, anos(receitas.map { it.data }), { mes = it }, { ano = it })
            SearchField(busca, { busca = it }, Modifier.padding(horizontal = 16.dp, vertical = 8.dp), "Buscar receita")
            TotalComExport(
                total = Moeda.formatar(total),
                cor = MaterialTheme.colorScheme.primary,
                onExport = { Exportacao.compartilharReceitas(context, filtradas) }
            )
            if (filtradas.isEmpty()) {
                EmptyState("Nenhuma receita no período.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtradas, key = { it.id }) { r ->
                        AppCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = r.cliente.ifBlank { r.descricao.ifBlank { "Receita" } },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = Datas.paraBR(r.data),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = Moeda.formatar(r.valor),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (r.descricao.isNotBlank()) InfoRow("Descrição", r.descricao)
                            if (r.formaPagamento.isNotBlank()) InfoRow("Pagamento", r.formaPagamento)
                            if (r.numeroNota.isNotBlank()) InfoRow("Nota fiscal", r.numeroNota)
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                IconButton(onClick = { editando = r; mostrarForm = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                                IconButton(onClick = { excluindo = r }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir")
                                }
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { editando = null; mostrarForm = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "Nova receita") }
    }

    if (mostrarForm) {
        ReceitaForm(
            inicial = editando,
            onSalvar = { viewModel.salvarReceita(it); mostrarForm = false },
            onDismiss = { mostrarForm = false }
        )
    }
    excluindo?.let { alvo ->
        ConfirmDeleteDialog(
            titulo = alvo.cliente.ifBlank { "receita" },
            onConfirmar = { viewModel.excluirReceita(alvo); excluindo = null },
            onCancelar = { excluindo = null }
        )
    }
}

@Composable
private fun ReceitaForm(
    inicial: Receita?,
    onSalvar: (Receita) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val config = remember {
        (context.applicationContext as GestorMeiApplication).container.configManager
    }

    var data by remember { mutableStateOf(inicial?.data ?: Datas.hoje()) }
    var cliente by remember { mutableStateOf(inicial?.cliente ?: "") }
    var descricao by remember { mutableStateOf(inicial?.descricao ?: "") }
    var valor by remember { mutableStateOf(inicial?.valor?.takeIf { it > 0 }?.toString() ?: "") }
    var forma by remember { mutableStateOf(inicial?.formaPagamento ?: Opcoes.formasPagamento.first()) }
    var nota by remember { mutableStateOf(inicial?.numeroNota ?: "") }
    var analisando by remember { mutableStateOf(false) }
    var mensagemIa by remember { mutableStateOf<String?>(null) }

    val seletorPdf = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            if (config.iaAnexosAtiva && config.temChave) {
                analisando = true
                mensagemIa = "Lendo a NF com a IA..."
                scope.launch {
                    val resultado = OpenAiService.extrairDeDespesaPdf(
                        context, uri, config.openAiKey, config.openAiModel
                    )
                    analisando = false
                    resultado.onSuccess { extra ->
                        extra.valor?.let { if (it > 0) valor = it.toString() }
                        extra.fornecedor?.let { if (it.isNotBlank()) cliente = it }
                        extra.data?.let { d -> Datas.normalizarParaIso(d)?.let { data = it } }
                        extra.descricao?.let { if (it.isNotBlank() && descricao.isBlank()) descricao = it }
                        mensagemIa = "Dados preenchidos pela IA. Confira antes de salvar."
                    }.onFailure {
                        mensagemIa = "Não foi possível ler com IA: ${it.message}"
                    }
                }
            } else {
                mensagemIa = "Configure a chave da OpenAI em Configurações para ler a NF."
            }
        }
    }

    FormDialog(
        titulo = if (inicial == null) "Nova receita" else "Editar receita",
        salvarHabilitado = valor.isNotBlank() && !analisando,
        onSalvar = {
            onSalvar(
                (inicial ?: Receita(empresaId = 0)).copy(
                    data = data,
                    cliente = cliente.trim(),
                    descricao = descricao.trim(),
                    valor = Moeda.parse(valor),
                    formaPagamento = forma,
                    numeroNota = nota.trim()
                )
            )
        },
        onDismiss = onDismiss
    ) {
        OutlinedButton(
            onClick = { seletorPdf.launch(arrayOf("application/pdf")) },
            enabled = !analisando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (analisando) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Text("  Lendo com IA...")
            } else {
                Text("Ler NF (PDF) com IA")
            }
        }
        mensagemIa?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        DatePickerField("Data", data, { data = it })
        FormTextField("Cliente", cliente, { cliente = it })
        FormTextField("Descrição do serviço/produto", descricao, { descricao = it }, singleLine = false, minLines = 2)
        FormTextField("Valor (R$)", valor, { valor = it })
        DropdownField("Forma de pagamento", Opcoes.formasPagamento, forma, { forma = it })
        FormTextField("Número da nota (opcional)", nota, { nota = it })
    }
}

@Composable
private fun DespesasTab(viewModel: FinanceiroViewModel) {
    val despesas by viewModel.despesas.collectAsStateWithLifecycle()
    var mes by remember { mutableIntStateOf(0) }
    var ano by remember { mutableStateOf("Todos") }
    var editando by remember { mutableStateOf<Despesa?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }
    var excluindo by remember { mutableStateOf<Despesa?>(null) }
    var busca by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filtradas = remember(despesas, mes, ano, busca) {
        despesas.filter { combinaPeriodo(it.data, mes, ano) }
            .filter {
                busca.isBlank() || it.fornecedor.contains(busca, true) ||
                    it.descricao.contains(busca, true) || it.categoria.contains(busca, true)
            }
    }
    val total = filtradas.sumOf { it.valor }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(12.dp))
            FiltroPeriodo(mes, ano, anos(despesas.map { it.data }), { mes = it }, { ano = it })
            SearchField(busca, { busca = it }, Modifier.padding(horizontal = 16.dp, vertical = 8.dp), "Buscar despesa")
            TotalComExport(
                total = Moeda.formatar(total),
                cor = MaterialTheme.colorScheme.error,
                onExport = { Exportacao.compartilharDespesas(context, filtradas) }
            )
            if (filtradas.isEmpty()) {
                EmptyState("Nenhuma despesa no período.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtradas, key = { it.id }) { d ->
                        AppCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = d.fornecedor.ifBlank { d.descricao.ifBlank { "Despesa" } },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = Datas.paraBR(d.data),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = Moeda.formatar(d.valor),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            if (d.descricao.isNotBlank()) InfoRow("Descrição", d.descricao)
                            if (d.categoria.isNotBlank()) InfoRow("Categoria", d.categoria)
                            if (d.numeroNota.isNotBlank()) InfoRow("Nota fiscal", d.numeroNota)
                            if (d.anexoUri.isNotBlank()) InfoRow("Anexo", "PDF")
                            if (d.origem.isNotBlank()) InfoRow("Origem", d.origem)
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                IconButton(onClick = { editando = d; mostrarForm = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                                IconButton(onClick = { excluindo = d }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir")
                                }
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { editando = null; mostrarForm = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "Nova despesa") }
    }

    if (mostrarForm) {
        DespesaForm(
            inicial = editando,
            onSalvar = { viewModel.salvarDespesa(it); mostrarForm = false },
            onDismiss = { mostrarForm = false }
        )
    }
    excluindo?.let { alvo ->
        ConfirmDeleteDialog(
            titulo = alvo.fornecedor.ifBlank { "despesa" },
            onConfirmar = { viewModel.excluirDespesa(alvo); excluindo = null },
            onCancelar = { excluindo = null }
        )
    }
}

@Composable
private fun DespesaForm(
    inicial: Despesa?,
    onSalvar: (Despesa) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val config = remember {
        (context.applicationContext as GestorMeiApplication).container.configManager
    }

    var data by remember { mutableStateOf(inicial?.data ?: Datas.hoje()) }
    var fornecedor by remember { mutableStateOf(inicial?.fornecedor ?: "") }
    var descricao by remember { mutableStateOf(inicial?.descricao ?: "") }
    var categoria by remember { mutableStateOf(inicial?.categoria ?: Opcoes.categoriasDespesa.first()) }
    var valor by remember { mutableStateOf(inicial?.valor?.takeIf { it > 0 }?.toString() ?: "") }
    var nota by remember { mutableStateOf(inicial?.numeroNota ?: "") }
    var anexoUri by remember { mutableStateOf(inicial?.anexoUri ?: "") }
    var analisando by remember { mutableStateOf(false) }
    var mensagemIa by remember { mutableStateOf<String?>(null) }

    val seletorPdf = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            anexoUri = uri.toString()
            if (config.iaAnexosAtiva && config.temChave) {
                analisando = true
                mensagemIa = "Lendo o PDF com a IA..."
                scope.launch {
                    val resultado = OpenAiService.extrairDeDespesaPdf(
                        context, uri, config.openAiKey, config.openAiModel
                    )
                    analisando = false
                    resultado.onSuccess { extra ->
                        extra.valor?.let { if (it > 0) valor = it.toString() }
                        extra.fornecedor?.let { if (it.isNotBlank()) fornecedor = it }
                        extra.data?.let { d -> Datas.normalizarParaIso(d)?.let { data = it } }
                        extra.descricao?.let { if (it.isNotBlank() && descricao.isBlank()) descricao = it }
                        extra.categoria?.let { c ->
                            if (Opcoes.categoriasDespesa.contains(c)) categoria = c
                        }
                        mensagemIa = "Dados preenchidos pela IA. Confira antes de salvar."
                    }.onFailure {
                        mensagemIa = "Não foi possível ler com IA: ${it.message}"
                    }
                }
            } else {
                mensagemIa = "PDF anexado. Configure a chave da OpenAI para leitura automática."
            }
        }
    }

    FormDialog(
        titulo = if (inicial == null) "Nova despesa" else "Editar despesa",
        salvarHabilitado = valor.isNotBlank() && !analisando,
        onSalvar = {
            onSalvar(
                (inicial ?: Despesa(empresaId = 0)).copy(
                    data = data,
                    fornecedor = fornecedor.trim(),
                    descricao = descricao.trim(),
                    categoria = categoria,
                    valor = Moeda.parse(valor),
                    numeroNota = nota.trim(),
                    anexoUri = anexoUri
                )
            )
        },
        onDismiss = onDismiss
    ) {
        OutlinedButton(
            onClick = { seletorPdf.launch(arrayOf("application/pdf")) },
            enabled = !analisando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (analisando) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Text("  Lendo com IA...")
            } else {
                Text(if (anexoUri.isBlank()) "Anexar PDF (nota/recibo)" else "PDF anexado — trocar")
            }
        }
        mensagemIa?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        DatePickerField("Data", data, { data = it })
        FormTextField("Fornecedor", fornecedor, { fornecedor = it })
        FormTextField("Descrição", descricao, { descricao = it }, singleLine = false, minLines = 2)
        DropdownField("Categoria", Opcoes.categoriasDespesa, categoria, { categoria = it })
        FormTextField("Valor (R$)", valor, { valor = it })
        FormTextField("Número da nota (opcional)", nota, { nota = it })
    }
}
