package com.gestormei.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/** Bloqueio do cofre de senhas por biometria/credencial do aparelho. */
object BiometricAuth {

    fun disponivel(context: Context): Boolean = runCatching {
        BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }.getOrDefault(false)

    fun autenticar(
        activity: FragmentActivity,
        onSucesso: () -> Unit,
        onErro: (String) -> Unit
    ) = runCatching {
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSucesso()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onErro(errString.toString())
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Desbloquear cofre")
            .setSubtitle("Confirme sua identidade para ver os acessos")
            .setNegativeButtonText("Cancelar")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
        prompt.authenticate(info)
    }.onFailure { onErro(it.message ?: "Falha na autenticação") }
}
