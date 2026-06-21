package com.gestormei.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/** Ações rápidas: copiar para a área de transferência, ligar e enviar e-mail. */
object Acoes {

    fun copiar(context: Context, rotulo: String, texto: String) {
        if (texto.isBlank()) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(rotulo, texto))
        Toast.makeText(context, "$rotulo copiado", Toast.LENGTH_SHORT).show()
    }

    fun ligar(context: Context, telefone: String) {
        val numero = telefone.filter { it.isDigit() || it == '+' }
        if (numero.isBlank()) return
        abrir(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$numero")))
    }

    fun email(context: Context, email: String) {
        if (email.isBlank()) return
        abrir(context, Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")))
    }

    private fun abrir(context: Context, intent: Intent) {
        runCatching {
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(context, "Nenhum app disponível para esta ação", Toast.LENGTH_SHORT).show()
        }
    }
}
