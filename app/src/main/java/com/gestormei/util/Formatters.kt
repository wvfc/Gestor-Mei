package com.gestormei.util

import java.text.Normalizer
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val localeBR = Locale("pt", "BR")

/** Remove acentos para comparações/buscas. */
fun String.semAcento(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")

/** Busca tolerante a acentos e maiúsculas/minúsculas. */
fun String.contemBusca(query: String): Boolean =
    this.semAcento().lowercase().contains(query.semAcento().lowercase())

object Moeda {
    private val format: NumberFormat = NumberFormat.getCurrencyInstance(localeBR)

    fun formatar(valor: Double): String = format.format(valor)

    /**
     * Converte texto em Double aceitando padrão brasileiro ("1.234,56"),
     * separador de milhar sem decimais ("1.000" = mil) e ponto decimal de
     * máquina ("1234.56", "990.00"), inclusive valores negativos.
     */
    fun parse(texto: String): Double {
        if (texto.isBlank()) return 0.0
        val limpo = texto.trim()
            .replace("R$", "")
            .replace(" ", "")
        return when {
            limpo.contains(",") ->
                limpo.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
            // só pontos como separador de milhar: 1.000 / 1.500.000
            Regex("^-?\\d{1,3}(\\.\\d{3})+$").matches(limpo) ->
                limpo.replace(".", "").toDoubleOrNull() ?: 0.0
            else -> limpo.toDoubleOrNull() ?: 0.0
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

    /** Tenta normalizar datas em vários formatos comuns para ISO (yyyy-MM-dd). */
    fun normalizarParaIso(texto: String): String? {
        val limpo = texto.trim()
        if (limpo.isBlank()) return null
        parse(limpo)?.let { return it.format(iso) }
        val formatos = listOf("dd/MM/yyyy", "dd-MM-yyyy", "yyyy/MM/dd", "dd/MM/yy")
        for (f in formatos) {
            runCatching {
                LocalDate.parse(limpo, DateTimeFormatter.ofPattern(f))
            }.getOrNull()?.let { return it.format(iso) }
        }
        return null
    }

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
