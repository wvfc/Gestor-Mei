package com.gestormei.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gestormei.ui.theme.Verde
import com.gestormei.ui.theme.Vermelho
import com.gestormei.util.Moeda
import kotlin.math.atan2

private val mesesIniciais = listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
private val mesesAbrev = listOf(
    "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
    "Jul", "Ago", "Set", "Out", "Nov", "Dez"
)

val paletaGraficos = listOf(
    Color(0xFF0F766E), Color(0xFF1D4ED8), Color(0xFFCA8A04), Color(0xFFDC2626),
    Color(0xFF7C3AED), Color(0xFF0891B2), Color(0xFFDB2777), Color(0xFF65A30D),
    Color(0xFFEA580C), Color(0xFF475569)
)

data class FatiaCategoria(val label: String, val valor: Double, val cor: Color)

@Composable
private fun LegendaItem(cor: Color, texto: String, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(cor)
        )
        Spacer(Modifier.size(6.dp))
        Text(texto, style = MaterialTheme.typography.bodySmall)
    }
}

/** Gráfico de barras mensal interativo: toque em um mês para ver os valores. */
@Composable
fun GraficoBarrasMensal(
    receitasPorMes: List<Double>,
    despesasPorMes: List<Double>,
    modifier: Modifier = Modifier
) {
    var selecionado by remember { mutableIntStateOf(-1) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendaItem(Verde, "Receitas")
            LegendaItem(Vermelho, "Despesas")
        }
        Spacer(Modifier.size(6.dp))
        Text(
            text = if (selecionado in 0..11) {
                "${mesesAbrev[selecionado]} • Receita ${Moeda.formatar(receitasPorMes[selecionado])} • " +
                    "Despesa ${Moeda.formatar(despesasPorMes[selecionado])}"
            } else {
                "Toque em um mês para ver os valores"
            },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.size(8.dp))

        val corTrilha = MaterialTheme.colorScheme.surfaceVariant
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val grupo = size.width / 12f
                        selecionado = (offset.x / grupo).toInt().coerceIn(0, 11)
                    }
                }
        ) {
            val maxV = (receitasPorMes + despesasPorMes).maxOrNull()?.takeIf { it > 0 } ?: 1.0
            val grupo = size.width / 12f
            val barW = grupo * 0.30f
            for (i in 0..11) {
                val baseX = grupo * i + grupo * 0.12f
                val ativo = selecionado < 0 || selecionado == i
                val alpha = if (ativo) 1f else 0.30f

                // trilha de fundo
                drawRect(
                    color = corTrilha,
                    topLeft = Offset(baseX, 0f),
                    size = Size(barW * 2 + 3f, size.height)
                )
                val rh = (receitasPorMes[i] / maxV * size.height).toFloat()
                val dh = (despesasPorMes[i] / maxV * size.height).toFloat()
                drawRect(
                    color = Verde.copy(alpha = alpha),
                    topLeft = Offset(baseX, size.height - rh),
                    size = Size(barW, rh)
                )
                drawRect(
                    color = Vermelho.copy(alpha = alpha),
                    topLeft = Offset(baseX + barW + 3f, size.height - dh),
                    size = Size(barW, dh)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            mesesIniciais.forEach { letra ->
                Text(
                    text = letra,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Gráfico de rosca interativo: toque numa fatia ou na legenda para destacar. */
@Composable
fun GraficoDonutCategorias(
    dados: List<FatiaCategoria>,
    modifier: Modifier = Modifier
) {
    var selecionado by remember { mutableIntStateOf(-1) }
    val total = dados.sumOf { it.valor }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .pointerInput(dados) {
                        detectTapGestures { offset ->
                            if (total <= 0) return@detectTapGestures
                            val centro = Offset(size.width / 2f, size.height / 2f)
                            var ang = Math.toDegrees(
                                atan2(
                                    (offset.y - centro.y).toDouble(),
                                    (offset.x - centro.x).toDouble()
                                )
                            )
                            // converte para começar no topo (-90°) e ir no sentido horário
                            ang = (ang + 90.0 + 360.0) % 360.0
                            var acumulado = 0.0
                            for (i in dados.indices) {
                                val sweep = dados[i].valor / total * 360.0
                                if (ang >= acumulado && ang < acumulado + sweep) {
                                    selecionado = if (selecionado == i) -1 else i
                                    break
                                }
                                acumulado += sweep
                            }
                        }
                    }
            ) {
                if (total <= 0) return@Canvas
                val stroke = 42f
                val diametro = size.minDimension - stroke
                val topLeft = Offset(
                    (size.width - diametro) / 2f,
                    (size.height - diametro) / 2f
                )
                var start = -90f
                dados.forEachIndexed { i, fatia ->
                    val sweep = (fatia.valor / total * 360.0).toFloat()
                    val ativo = selecionado < 0 || selecionado == i
                    drawArc(
                        color = fatia.cor.copy(alpha = if (ativo) 1f else 0.30f),
                        startAngle = start,
                        sweepAngle = sweep - 1.5f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diametro, diametro),
                        style = Stroke(width = stroke)
                    )
                    start += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val foco = dados.getOrNull(selecionado)
                Text(
                    text = foco?.label ?: "Total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = Moeda.formatar(foco?.valor ?: total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.size(8.dp))
        dados.forEachIndexed { i, fatia ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selecionado = if (selecionado == i) -1 else i }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendaItem(fatia.cor, fatia.label, modifier = Modifier.weight(1f))
                Text(
                    text = Moeda.formatar(fatia.valor),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/** Medidor (gauge) semicircular do uso do limite anual do MEI. */
@Composable
fun GraficoGaugeLimite(
    percentual: Float,
    cor: Color,
    modifier: Modifier = Modifier
) {
    val p = percentual.coerceIn(0f, 1f)
    val corTrilha = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            val stroke = 30f
            val largura = size.width - stroke
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            val tamanho = Size(largura, (size.height - stroke) * 2)
            drawArc(
                color = corTrilha,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = topLeft,
                size = tamanho,
                style = Stroke(width = stroke)
            )
            drawArc(
                color = cor,
                startAngle = 180f,
                sweepAngle = 180f * p,
                useCenter = false,
                topLeft = topLeft,
                size = tamanho,
                style = Stroke(width = stroke)
            )
        }
        Text(
            text = "${(p * 100).toInt()}%",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = cor
        )
    }
}
