package com.gestormei

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.gestormei.data.AppContainer
import com.gestormei.notificacao.LembretesWorker
import com.gestormei.notificacao.Notificacoes
import java.util.concurrent.TimeUnit

class GestorMeiApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Notificacoes.criarCanal(this)
        agendarLembretes()
    }

    private fun agendarLembretes() {
        val pedido = PeriodicWorkRequestBuilder<LembretesWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "lembretes_diarios",
            ExistingPeriodicWorkPolicy.KEEP,
            pedido
        )
    }
}
