package com.gestormei

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gestormei.ui.screens.AcessosScreen
import com.gestormei.ui.screens.AgendaScreen
import com.gestormei.ui.screens.ClientesScreen
import com.gestormei.ui.screens.ConfiguracoesScreen
import com.gestormei.ui.screens.EmpresasScreen
import com.gestormei.ui.screens.FinanceiroScreen
import com.gestormei.ui.screens.HomeScreen
import com.gestormei.ui.screens.IdeiasScreen
import com.gestormei.ui.screens.ProjetosScreen
import com.gestormei.ui.screens.RelatoriosScreen
import com.gestormei.ui.theme.GestorMeiTheme
import com.gestormei.viewmodel.EmpresaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GestorMeiTheme {
                GestorMeiApp()
            }
        }
    }
}

private enum class Destino(val rota: String, val titulo: String, val icone: ImageVector) {
    INICIO("inicio", "Início", Icons.Default.Home),
    FINANCEIRO("financeiro", "Financeiro", Icons.Default.AttachMoney),
    PROJETOS("projetos", "Projetos", Icons.Default.Work),
    AGENDA("agenda", "Agenda", Icons.Default.Event),
    ACESSOS("acessos", "Acessos", Icons.Default.Lock),
    IDEIAS("ideias", "Ideias", Icons.Default.Lightbulb),
    EMPRESAS("empresas", "Empresas", Icons.Default.Apartment)
}

private const val ROTA_RELATORIOS = "relatorios"
private const val ROTA_CONFIGURACOES = "configuracoes"
private const val ROTA_CLIENTES = "clientes"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GestorMeiApp() {
    val navController = rememberNavController()
    val empresaVM: EmpresaViewModel = viewModel()
    val empresas by empresaVM.empresas.collectAsStateWithLifecycle()
    val selecionadaId by empresaVM.empresaSelecionadaId.collectAsStateWithLifecycle()

    val backStack by navController.currentBackStackEntryAsState()
    val rotaAtual = backStack?.destination?.route

    val empresaAtiva = empresas.firstOrNull { it.id == selecionadaId } ?: empresas.firstOrNull()
    var menuAberto by remember { mutableStateOf(false) }
    val ehDetalhe = rotaAtual in setOf(ROTA_RELATORIOS, ROTA_CONFIGURACOES, ROTA_CLIENTES)

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (ehDetalhe) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                title = {
                    if (empresas.size > 1) {
                        TextButton(onClick = { menuAberto = true }) {
                            Text(
                                text = empresaAtiva?.nomeFantasia ?: "Gestor MEI",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Trocar empresa",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        DropdownMenu(expanded = menuAberto, onDismissRequest = { menuAberto = false }) {
                            empresas.forEach { empresa ->
                                DropdownMenuItem(
                                    text = { Text(empresa.nomeFantasia) },
                                    onClick = {
                                        empresaVM.selecionar(empresa.id)
                                        menuAberto = false
                                    }
                                )
                            }
                        }
                    } else {
                        Text(
                            text = empresaAtiva?.nomeFantasia ?: "Gestor MEI",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    androidx.compose.material3.IconButton(
                        onClick = { navController.navigate(ROTA_CONFIGURACOES) }
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Configurações",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                Destino.entries.forEach { destino ->
                    val selecionado = backStack?.destination?.hierarchy?.any { it.route == destino.rota } == true
                    NavigationBarItem(
                        selected = selecionado,
                        onClick = {
                            if (!selecionado) {
                                navController.navigate(destino.rota) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = { Icon(destino.icone, contentDescription = destino.titulo) },
                        label = { Text(destino.titulo, maxLines = 1) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destino.INICIO.rota,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            composable(Destino.INICIO.rota) {
                HomeScreen(
                    onAbrirRelatorios = { navController.navigate(ROTA_RELATORIOS) },
                    onIrParaEmpresas = { navController.navigate(Destino.EMPRESAS.rota) }
                )
            }
            composable(Destino.FINANCEIRO.rota) { FinanceiroScreen() }
            composable(Destino.PROJETOS.rota) { ProjetosScreen() }
            composable(Destino.AGENDA.rota) { AgendaScreen() }
            composable(Destino.ACESSOS.rota) { AcessosScreen() }
            composable(Destino.IDEIAS.rota) { IdeiasScreen() }
            composable(Destino.EMPRESAS.rota) { EmpresasScreen() }
            composable(ROTA_RELATORIOS) {
                RelatoriosScreen(onVoltar = { navController.popBackStack() })
            }
            composable(ROTA_CONFIGURACOES) {
                ConfiguracoesScreen(
                    onAbrirClientes = { navController.navigate(ROTA_CLIENTES) },
                    onVoltar = { navController.popBackStack() }
                )
            }
            composable(ROTA_CLIENTES) { ClientesScreen() }
        }
    }
}
