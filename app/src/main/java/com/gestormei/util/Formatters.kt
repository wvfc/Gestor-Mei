package com.gestormei.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val localeBR = Locale("pt", "BR")

object Moeda {
    private val format: NumberFormat = NumberFormat.getCurrencyInstance(localeBR)

    fun formatar(valor: Double): String = format.format(valor)

    /** Converte texto digitado ("1.234,56" ou "1234.56") em Double. */
    fun parse(texto: String): Double {
        if (texto.isBlank()) return 0.0
        val limpo = texto.trim()
            .replace("R$", "")
            .replace(" ", "")
        return if (limpo.contains(",")) {
            limpo.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
        } else {
            limpo.toDoubleOrNull() ?: 0.0
        }
    }
}

object Datas {
    private val iso: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val br: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun hoje(): String = LocalDate.now().format(iso)

    fun parse(texto: String): LocalDate? =
        runCatching { LocalDate.parse(texto, iso) }.getOrNull()

    fun paraBR(texto: String): String =
        parse(texto)?.format(br) ?: texto

    fun deMillisParaIso(millis: Long): String =
        java.time.Instant.ofEpochMilli(millis)
            .atZone(java.time.ZoneOffset.UTC)
            .toLocalDate()
            .format(iso)
}

object Opcoes {
    val formasPagamento = listOf("Pix", "Dinheiro", "Cartão de Crédito", "Cartão de Débito", "Boleto", "Transferência")
    val categoriasDespesa = listOf("Material", "Serviços", "Impostos", "Aluguel", "Transporte", "Marketing", "Equipamentos", "Outros")
    val statusCompromisso = listOf("Pendente", "Concluído", "Cancelado")
    val statusProjeto = listOf("Ideia", "Em andamento", "Aguardando cliente", "Finalizado", "Cancelado")
    val prioridades = listOf("Baixa", "Média", "Alta")
    val statusIdeia = listOf("Nova", "Em análise", "Aplicada", "Descartada")
    val meses = listOf(
        "Todos", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    )
}
