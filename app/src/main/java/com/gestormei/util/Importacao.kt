package com.gestormei.util

import android.content.Context
import android.net.Uri
import com.gestormei.data.model.Cliente
import com.gestormei.data.model.Despesa
import com.gestormei.data.model.Receita
import kotlin.math.abs

/**
 * Importação de dados a partir de arquivos CSV:
 * - lista de clientes (colunas: nome, email, telefone);
 * - extrato/fatura do Nubank (colunas com data, descrição/título e valor).
 */
object Importacao {

    data class Resultado<T>(val itens: List<T>, val ignorados: Int)

    fun lerLinhas(context: Context, uri: Uri): List<String> =
        context.contentResolver.openInputStream(uri)
            ?.bufferedReader()
            ?.use { leitor -> leitor.readLines() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

    fun clientesDeCsv(linhas: List<String>, empresaId: Long): Resultado<Cliente> {
        if (linhas.isEmpty()) return Resultado(emptyList(), 0)
        val delim = delimitador(linhas.first())
        val primeira = campos(linhas.first(), delim).map { it.lowercase() }
        val temCabecalho = primeira.any { it.contains("nome") || it.contains("email") || it.contains("e-mail") }

        val idxNome: Int
        val idxEmail: Int
        val idxTel: Int
        if (temCabecalho) {
            idxNome = primeira.indexOfFirst { it.contains("nome") }.let { if (it >= 0) it else 0 }
            idxEmail = primeira.indexOfFirst { it.contains("mail") }
            idxTel = primeira.indexOfFirst { it.contains("tel") || it.contains("fone") || it.contains("cel") }
        } else {
            idxNome = 0; idxEmail = 1; idxTel = 2
        }

        val dados = if (temCabecalho) linhas.drop(1) else linhas
        val clientes = mutableListOf<Cliente>()
        var ignorados = 0
        for (linha in dados) {
            val c = campos(linha, delim)
            val nome = c.getOrNull(idxNome)?.trim().orEmpty()
            if (nome.isBlank()) { ignorados++; continue }
            clientes.add(
                Cliente(
                    empresaId = empresaId,
                    nome = nome,
                    email = c.getOrNull(idxEmail)?.trim().orEmpty(),
                    telefone = c.getOrNull(idxTel)?.trim().orEmpty()
                )
            )
        }
        return Resultado(clientes, ignorados)
    }

    data class ResultadoNubank(
        val receitas: List<Receita>,
        val despesas: List<Despesa>,
        val ignorados: Int
    )

    /**
     * Importa o extrato de conta do Nubank (colunas: Data, Valor, Identificador,
     * Descrição). Valores positivos viram receitas; negativos viram despesas.
     * O Identificador é guardado como referência para evitar duplicatas.
     */
    fun extratoNubank(linhas: List<String>, empresaId: Long): ResultadoNubank {
        if (linhas.isEmpty()) return ResultadoNubank(emptyList(), emptyList(), 0)
        val delim = delimitador(linhas.first())
        val cabecalho = campos(linhas.first(), delim).map { it.lowercase() }
        val temCabecalho = cabecalho.any { it.contains("data") || it.contains("date") } &&
            cabecalho.any { it.contains("valor") || it.contains("amount") }

        val idxData: Int
        val idxValor: Int
        val idxId: Int
        val idxDesc: Int
        if (temCabecalho) {
            idxData = cabecalho.indexOfFirst { it.contains("data") || it.contains("date") }
            idxValor = cabecalho.indexOfFirst { it.contains("valor") || it.contains("amount") }
            idxId = cabecalho.indexOfFirst { it.contains("identificador") || it == "id" }
            idxDesc = cabecalho.indexOfFirst {
                it.contains("descri") || it.contains("title") || it.contains("histórico") ||
                    it.contains("historico") || it.contains("estabelecimento")
            }.let { if (it >= 0) it else cabecalho.lastIndex }
        } else {
            idxData = 0; idxValor = 1; idxId = 2; idxDesc = 3
        }

        val dados = if (temCabecalho) linhas.drop(1) else linhas
        val receitas = mutableListOf<Receita>()
        val despesas = mutableListOf<Despesa>()
        var ignorados = 0
        for (linha in dados) {
            val c = campos(linha, delim)
            val dataIso = c.getOrNull(idxData)?.let { Datas.normalizarParaIso(it) }
            val valorBruto = c.getOrNull(idxValor)?.let { Moeda.parse(it) } ?: 0.0
            // a descrição é a última coluna e pode conter o delimitador
            val descricao = if (idxDesc <= c.lastIndex) {
                c.subList(idxDesc, c.size).joinToString(delim.toString()).trim()
            } else ""
            val referencia = c.getOrNull(idxId)?.trim().orEmpty()
            if (dataIso == null || valorBruto == 0.0) { ignorados++; continue }

            val nome = contraparte(descricao)
            if (valorBruto > 0) {
                receitas.add(
                    Receita(
                        empresaId = empresaId,
                        data = dataIso,
                        cliente = nome,
                        descricao = descricao,
                        valor = valorBruto,
                        formaPagamento = formaReceita(descricao),
                        origem = "Nubank",
                        referencia = referencia
                    )
                )
            } else {
                despesas.add(
                    Despesa(
                        empresaId = empresaId,
                        data = dataIso,
                        fornecedor = nome,
                        descricao = descricao,
                        categoria = categoriaDespesa(descricao),
                        valor = abs(valorBruto),
                        origem = "Nubank",
                        referencia = referencia
                    )
                )
            }
        }
        return ResultadoNubank(receitas, despesas, ignorados)
    }

    /** Extrai o nome da contraparte da descrição do Nubank (2º trecho após " - "). */
    private fun contraparte(descricao: String): String {
        val partes = descricao.split(" - ")
        val nome = partes.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() } ?: partes.first().trim()
        return nome.take(60)
    }

    private fun formaReceita(descricao: String): String = when {
        descricao.contains("Pix", ignoreCase = true) -> "Pix"
        descricao.contains("boleto", ignoreCase = true) -> "Boleto"
        else -> "Transferência"
    }

    private fun categoriaDespesa(descricao: String): String {
        val d = descricao.uppercase()
        return when {
            d.contains("DAS") || d.contains("SIMPLES NACIONAL") || d.contains("IMPOSTO") -> "Impostos"
            d.contains("TARIFA") -> "Serviços"
            d.contains("COMPRA NO DÉBITO") || d.contains("COMPRA NO DEBITO") -> "Material"
            d.contains("ALUGUEL") -> "Aluguel"
            else -> "Outros"
        }
    }

    private fun delimitador(linha: String): Char =
        if (linha.count { it == ';' } > linha.count { it == ',' }) ';' else ','

    /** Divide uma linha de CSV respeitando aspas duplas. */
    private fun campos(linha: String, delim: Char): List<String> {
        val resultado = mutableListOf<String>()
        val atual = StringBuilder()
        var dentroAspas = false
        for (ch in linha) {
            when {
                ch == '"' -> dentroAspas = !dentroAspas
                ch == delim && !dentroAspas -> {
                    resultado.add(atual.toString()); atual.clear()
                }
                else -> atual.append(ch)
            }
        }
        resultado.add(atual.toString())
        return resultado.map { it.trim().removeSurrounding("\"") }
    }
}
