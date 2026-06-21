package com.gestormei.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Configurações do app guardadas localmente de forma criptografada
 * (EncryptedSharedPreferences). Se a criptografia falhar no dispositivo,
 * cai para SharedPreferences comum para não travar o app.
 */
class ConfigManager(context: Context) {

    private val prefs: SharedPreferences = runCatching {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "gestor_mei_config_enc",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }.getOrElse {
        context.getSharedPreferences("gestor_mei_config", Context.MODE_PRIVATE)
    }

    var openAiKey: String
        get() = prefs.getString(KEY_OPENAI, "") ?: ""
        set(value) = prefs.edit().putString(KEY_OPENAI, value.trim()).apply()

    var openAiModel: String
        get() = prefs.getString(KEY_MODELO, MODELO_PADRAO) ?: MODELO_PADRAO
        set(value) = prefs.edit().putString(KEY_MODELO, value.trim().ifBlank { MODELO_PADRAO }).apply()

    var iaAnexosAtiva: Boolean
        get() = prefs.getBoolean(KEY_IA_ANEXOS, true)
        set(value) = prefs.edit().putBoolean(KEY_IA_ANEXOS, value).apply()

    val temChave: Boolean get() = openAiKey.isNotBlank()

    companion object {
        const val MODELO_PADRAO = "gpt-4o-mini"
        val MODELOS = listOf("gpt-4o-mini", "gpt-4o")
        private const val KEY_OPENAI = "openai_key"
        private const val KEY_MODELO = "openai_model"
        private const val KEY_IA_ANEXOS = "ia_anexos"
    }
}
