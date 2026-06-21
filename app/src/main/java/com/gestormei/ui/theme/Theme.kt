package com.gestormei.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = TealLight,
    onPrimaryContainer = TealDark,
    secondary = Azul,
    onSecondary = Color.White,
    secondaryContainer = AzulLight,
    background = Fundo,
    onBackground = TextoPrincipal,
    surface = Superficie,
    onSurface = TextoPrincipal,
    surfaceVariant = Fundo,
    onSurfaceVariant = TextoSecundario,
    error = Vermelho
)

@Composable
fun GestorMeiTheme(content: @Composable () -> Unit) {
    // Tema claro fixo, conforme requisito de interface limpa e profissional.
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography(),
        content = content
    )
}
