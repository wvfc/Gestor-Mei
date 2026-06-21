package com.gestormei.util

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

/**
 * Leitor mínimo de planilhas .xlsx (Excel) sem bibliotecas externas.
 * Um .xlsx é um zip com XMLs; lemos sharedStrings.xml e a primeira planilha.
 * Retorna uma lista de linhas, cada linha como mapa "Letra da coluna" -> valor.
 */
object XlsxReader {

    fun ehXlsx(bytes: ByteArray): Boolean =
        bytes.size >= 4 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte() &&
            bytes[2] == 3.toByte() && bytes[3] == 4.toByte()

    fun lerGrade(bytes: ByteArray): List<Map<String, String>> {
        val entradas = descompactar(bytes)
        val shared = entradas["xl/sharedStrings.xml"]?.let { parseSharedStrings(it) } ?: emptyList()
        val nomePlanilha = entradas.keys
            .filter { it.startsWith("xl/worksheets/sheet") && it.endsWith(".xml") }
            .minOrNull() ?: return emptyList()
        return parsePlanilha(entradas[nomePlanilha]!!, shared)
    }

    private fun descompactar(bytes: ByteArray): Map<String, ByteArray> {
        val mapa = HashMap<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            var entrada = zis.nextEntry
            while (entrada != null) {
                if (!entrada.isDirectory) mapa[entrada.name] = zis.readBytes()
                zis.closeEntry()
                entrada = zis.nextEntry
            }
        }
        return mapa
    }

    private fun parseSharedStrings(bytes: ByteArray): List<String> {
        val lista = ArrayList<String>()
        val parser = Xml.newPullParser().apply { setInput(ByteArrayInputStream(bytes), "UTF-8") }
        val sb = StringBuilder()
        var dentroSi = false
        var dentroT = false
        var evt = parser.eventType
        while (evt != XmlPullParser.END_DOCUMENT) {
            when (evt) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "si" -> { dentroSi = true; sb.setLength(0) }
                    "t" -> if (dentroSi) dentroT = true
                }
                XmlPullParser.TEXT -> if (dentroT) sb.append(parser.text)
                XmlPullParser.END_TAG -> when (parser.name) {
                    "t" -> dentroT = false
                    "si" -> { lista.add(sb.toString()); dentroSi = false }
                }
            }
            evt = parser.next()
        }
        return lista
    }

    private fun parsePlanilha(bytes: ByteArray, shared: List<String>): List<Map<String, String>> {
        val linhas = ArrayList<Map<String, String>>()
        val parser = Xml.newPullParser().apply { setInput(ByteArrayInputStream(bytes), "UTF-8") }
        var linhaAtual: LinkedHashMap<String, String>? = null
        var coluna = ""
        var tipo = ""
        var dentroValor = false
        val sb = StringBuilder()
        var evt = parser.eventType
        while (evt != XmlPullParser.END_DOCUMENT) {
            when (evt) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "row" -> linhaAtual = LinkedHashMap()
                    "c" -> {
                        coluna = parser.getAttributeValue(null, "r")?.takeWhile { it.isLetter() } ?: ""
                        tipo = parser.getAttributeValue(null, "t") ?: ""
                    }
                    "v", "t" -> { dentroValor = true; sb.setLength(0) }
                }
                XmlPullParser.TEXT -> if (dentroValor) sb.append(parser.text)
                XmlPullParser.END_TAG -> when (parser.name) {
                    "v", "t" -> {
                        if (dentroValor) {
                            val bruto = sb.toString()
                            val valor = if (tipo == "s") {
                                shared.getOrNull(bruto.toIntOrNull() ?: -1) ?: ""
                            } else bruto
                            if (coluna.isNotEmpty()) linhaAtual?.put(coluna, valor)
                            dentroValor = false
                        }
                    }
                    "row" -> { linhaAtual?.let { linhas.add(it) }; linhaAtual = null }
                }
            }
            evt = parser.next()
        }
        return linhas
    }
}
