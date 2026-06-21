package com.gestormei.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gestormei.data.ConfigManager
import com.gestormei.ui.components.AppCard
import com.gestormei.ui.components.DropdownField
import com.gestormei.ui.components.SectionTitle
import com.gestormei.viewmodel.ConfigViewModel

@Composable
fun ConfiguracoesScreen(
    onAbrirClientes: () -> Unit,
    onVoltar: () -> Unit,
    viewModel: ConfigViewModel = viewModel()
) {
    val mensagem by viewModel.mensagem.collectAsStateWithLifecycle()
    var chaveVisivel by remember { mutableStateOf(false) }

    val importarClientes = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importarClientes(it) } }

    val importarNubank = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importarExtratoNubank(it) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Configurações",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // 1) Entrada da IA da OpenAI
        AppCard {
            SectionTitle("Inteligência Artificial (OpenAI)")
            Text(
                "Informe sua chave da API. Ela fica guardada apenas neste aparelho. " +
                    "Ao anexar um PDF em uma despesa, a IA lê o documento e preenche o valor automaticamente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.openAiKey,
                onValueChange = { viewModel.salvarChave(it) },
                label = { Text("Chave da API (sk-...)") },
                singleLine = true,
                visualTransformation = if (chaveVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { chaveVisivel = !chaveVisivel }) {
                        Icon(
                            imageVector = if (chaveVisivel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            DropdownField(
                label = "Modelo",
                options = ConfigManager.MODELOS,
                selected = viewModel.openAiModel,
                onSelected = { viewModel.selecionarModelo(it) }
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ler anexos PDF com IA", modifier = Modifier.weight(1f))
                Switch(
                    checked = viewModel.iaAnexosAtiva,
                    onCheckedChange = { viewModel.alternarIaAnexos(it) }
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Privacidade: ao usar a leitura por IA, a imagem do documento é enviada " +
                    "aos servidores da OpenAI para processamento. Desative a opção acima se " +
                    "não quiser enviar seus documentos.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 2) Entrada de importação de clientes
        AppCard {
            SectionTitle("Importar lista de clientes")
            Text(
                "Aceita planilha do Excel (.xlsx) ou CSV. Reconhece as colunas " +
                    "Nome/Razão Social, CNPJ, Telefone/Celular e Email.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    importarClientes.launch(
                        arrayOf(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "application/vnd.ms-excel",
                            "text/csv", "text/*", "application/octet-stream", "*/*"
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Text("  Selecionar arquivo (XLSX/CSV)")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onAbrirClientes, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Group, contentDescription = null)
                Text("  Ver clientes")
            }
        }

        // 3) Entrada de importação do extrato do Nubank
        AppCard {
            SectionTitle("Importar extrato do Nubank")
            Text(
                "Arquivo CSV do extrato do Nubank (Data, Valor, Identificador, Descrição). " +
                    "Valores positivos viram receitas e negativos viram despesas, vinculadas à " +
                    "empresa selecionada. Lançamentos repetidos são ignorados.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { importarNubank.launch(arrayOf("text/*", "text/csv", "application/octet-stream", "*/*")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Text("  Selecionar extrato (CSV)")
            }
        }

        OutlinedButton(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar")
        }
        Spacer(Modifier.height(8.dp))
    }

    mensagem?.let { texto ->
        AlertDialog(
            onDismissRequest = { viewModel.limparMensagem() },
            confirmButton = {
                TextButton(onClick = { viewModel.limparMensagem() }) { Text("OK") }
            },
            title = { Text("Importação") },
            text = { Text(texto) }
        )
    }
}
