package com.gestormei.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/** Resultado da leitura de um documento de despesa pela IA. */
data class ExtracaoDespesa(
    val valor: Double? = null,
    val fornecedor: String? = null,
    val data: String? = null,
    val descricao: String? = null,
    val categoria: String? = null
)

/**
 * Integração com a API da OpenAI. Renderiza a 1ª página do PDF anexado como
 * imagem e usa um modelo com visão para extrair os dados da despesa.
 *
 * A chave da API é informada pelo usuário em Configurações e fica apenas no
 * dispositivo. As chamadas só acontecem quando há chave configurada.
 */
object OpenAiService {

    private const val ENDPOINT = "https://api.openai.com/v1/chat/completions"
    private const val PROMPT = "Você é um assistente que lê notas fiscais, recibos e cupons. " +
        "Extraia os dados da despesa da imagem e responda APENAS um JSON com as chaves: " +
        "valor (número, o valor total pago, use ponto como separador decimal), " +
        "fornecedor (texto), data (formato yyyy-MM-dd), descricao (texto curto), " +
        "categoria (uma de: Material, Serviços, Impostos, Aluguel, Transporte, Marketing, Equipamentos, Outros). " +
        "Se algum campo não for encontrado, use null."

    suspend fun extrairDeDespesaPdf(
        context: Context,
        uri: Uri,
        apiKey: String,
        modelo: String
    ): Result<ExtracaoDespesa> = withContext(Dispatchers.IO) {
        runCatching {
            val base64 = renderizarPrimeiraPagina(context, uri)
                ?: error("Não foi possível ler o PDF.")
            val conteudo = chamarVisao(apiKey, modelo, base64)
            parseResposta(conteudo)
        }
    }

    private fun renderizarPrimeiraPagina(context: Context, uri: Uri): String? {
        val pfd: ParcelFileDescriptor =
            context.contentResolver.openFileDescriptor(uri, "r") ?: return null
        pfd.use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                if (renderer.pageCount == 0) return null
                renderer.openPage(0).use { page ->
                    val escala = 2
                    val bitmap = Bitmap.createBitmap(
                        page.width * escala,
                        page.height * escala,
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    val saida = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, saida)
                    bitmap.recycle()
                    return Base64.encodeToString(saida.toByteArray(), Base64.NO_WRAP)
                }
            }
        }
    }

    private fun chamarVisao(apiKey: String, modelo: String, imagemBase64: String): String {
        val imagem = JSONObject()
            .put("type", "image_url")
            .put(
                "image_url",
                JSONObject().put("url", "data:image/jpeg;base64,$imagemBase64")
            )
        val texto = JSONObject().put("type", "text").put("text", PROMPT)
        val mensagemUsuario = JSONObject()
            .put("role", "user")
            .put("content", JSONArray().put(texto).put(imagem))

        val corpo = JSONObject()
            .put("model", modelo)
            .put("messages", JSONArray().put(mensagemUsuario))
            .put("max_tokens", 400)
            .put("temperature", 0)
            .put("response_format", JSONObject().put("type", "json_object"))

        val conexao = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000
            readTimeout = 45_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $apiKey")
        }
        conexao.outputStream.use { it.write(corpo.toString().toByteArray()) }

        val codigo = conexao.responseCode
        val resposta = (if (codigo in 200..299) conexao.inputStream else conexao.errorStream)
            ?.bufferedReader()?.use { it.readText() }.orEmpty()
        conexao.disconnect()

        if (codigo !in 200..299) {
            val msg = runCatching {
                JSONObject(resposta).getJSONObject("error").getString("message")
            }.getOrDefault("Erro $codigo na API da OpenAI")
            error(msg)
        }

        return JSONObject(resposta)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    private fun parseResposta(conteudo: String): ExtracaoDespesa {
        val json = JSONObject(conteudo)
        fun texto(chave: String): String? =
            if (json.has(chave) && !json.isNull(chave)) json.optString(chave).takeIf { it.isNotBlank() } else null
        val valor = if (json.has("valor") && !json.isNull("valor")) {
            json.optDouble("valor").takeIf { !it.isNaN() }
        } else null
        return ExtracaoDespesa(
            valor = valor,
            fornecedor = texto("fornecedor"),
            data = texto("data"),
            descricao = texto("descricao"),
            categoria = texto("categoria")
        )
    }
}
