package com.gestormei.notificacao

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gestormei.data.database.AppDatabase
import com.gestormei.util.Datas
import java.time.LocalDate

/** Verifica diariamente compromissos próximos e o vencimento do DAS. */
class LembretesWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val hoje = LocalDate.now()

        val proximos = db.compromissoDao().todas().filter {
            it.status == "Pendente" &&
                Datas.parse(it.data)?.let { d -> d == hoje || d == hoje.plusDays(1) } == true
        }
        if (proximos.isNotEmpty()) {
            Notificacoes.notificar(
                applicationContext,
                ID_AGENDA,
                "Compromissos próximos",
                "Você tem ${proximos.size} compromisso(s) para hoje/amanhã."
            )
        }

        if (hoje.dayOfMonth in 15..20) {
            val pagouDas = db.despesaDao().todas().any {
                it.categoria == "Impostos" &&
                    Datas.parse(it.data)?.let { d -> d.year == hoje.year && d.monthValue == hoje.monthValue } == true
            }
            if (!pagouDas) {
                Notificacoes.notificar(
                    applicationContext,
                    ID_DAS,
                    "DAS do MEI",
                    "Não esqueça de pagar o DAS deste mês (vence dia 20)."
                )
            }
        }
        return Result.success()
    }

    companion object {
        private const val ID_AGENDA = 1001
        private const val ID_DAS = 1002
    }
}
