package com.gestormei.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.gestormei.data.model.Despesa
import com.gestormei.data.model.Receita
import java.io.File

/** Exportação de dados em CSV e compartilhamento via apps do sistema. */
object Exportacao {

    fun compartilharReceitas(context: Context, receitas: List<Receita>) {
        val cabecalho = "Data;Cliente;Descricao;Valor;FormaPagamento;NotaFiscal;Origem"
        val linhas = receitas.joinToString("\n") { r ->
            listOf(
                r.data, r.cliente, r.descricao,
                formatarValor(r.valor), r.formaPagamento, r.numeroNota, r.origem
            ).joinToString(";") { escapar(it) }
        }
        compartilhar(context, "receitas.csv", "$cabecalho\n$linhas")
    }

    fun compartilharDespesas(context: Context, despesas: List<Despesa>) {
        val cabecalho = "Data;Fornecedor;Descricao;Categoria;Valor;NotaFiscal;Origem"
        val linhas = despesas.joinToString("\n") { d ->
            listOf(
                d.data, d.fornecedor, d.descricao, d.categoria,
                formatarValor(d.valor), d.numeroNota, d.origem
            ).joinToString(";") { escapar(it) }
        }
        compartilhar(context, "despesas.csv", "$cabecalho\n$linhas")
    }

    private fun compartilhar(context: Context, nomeArquivo: String, conteudo: String) {
        val dir = File(context.cacheDir, "exportados").apply { mkdirs() }
        val arquivo = File(dir, nomeArquivo)
        arquivo.writeText(conteudo, Charsets.UTF_8)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", arquivo)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, "Compartilhar CSV")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun formatarValor(valor: Double): String =
        String.format(java.util.Locale.US, "%.2f", valor)

    private fun escapar(texto: String): String =
        if (texto.contains(';') || texto.contains('"') || texto.contains('\n')) {
            "\"" + texto.replace("\"", "\"\"") + "\""
        } else texto
}
