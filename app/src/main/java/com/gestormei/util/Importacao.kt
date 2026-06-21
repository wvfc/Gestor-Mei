package com.gestormei.util

import android.content.Context
import android.net.Uri
import com.gestormei.data.model.Cliente
import com.gestormei.data.model.Despesa
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

    fun despesasNubankDeCsv(linhas: List<String>, empresaId: Long): Resultado<Despesa> {
        if (linhas.isEmpty()) return Resultado(emptyList(), 0)
        val delim = delimitador(linhas.first())
        val cabecalho = campos(linhas.first(), delim).map { it.lowercase() }
        val temCabecalho = cabecalho.any { it.contains("date") || it.contains("data") } &&
            cabecalho.any { it.contains("amount") || it.contains("valor") }

        val idxData: Int
        val idxValor: Int
        val idxDesc: Int
        if (temCabecalho) {
            idxData = cabecalho.indexOfFirst { it.contains("date") || it.contains("data") }
            idxValor = cabecalho.indexOfFirst { it.contains("amount") || it.contains("valor") }
            idxDesc = cabecalho.indexOfFirst {
                it.contains("title") || it.contains("descri") || it.contains("identificador") || it.contains("estabelecimento")
            }.let { if (it >= 0) it else 1 }
        } else {
            idxData = 0; idxDesc = 1; idxValor = 2
        }

        val dados = if (temCabecalho) linhas.drop(1) else linhas
        val despesas = mutableListOf<Despesa>()
        var ignorados = 0
        for (linha in dados) {
            val c = campos(linha, delim)
            val dataIso = c.getOrNull(idxData)?.let { Datas.normalizarParaIso(it) }
            val valorBruto = c.getOrNull(idxValor)?.let { Moeda.parse(it) } ?: 0.0
            val valor = abs(valorBruto)
            val descricao = c.getOrNull(idxDesc)?.trim().orEmpty()
            if (dataIso == null || valor <= 0.0) { ignorados++; continue }
            despesas.add(
                Despesa(
                    empresaId = empresaId,
                    data = dataIso,
                    fornecedor = descricao.take(60),
                    descricao = descricao,
                    categoria = "Outros",
                    valor = valor,
                    origem = "Nubank"
                )
            )
        }
        return Resultado(despesas, ignorados)
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
