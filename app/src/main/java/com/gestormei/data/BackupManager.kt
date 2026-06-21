package com.gestormei.data

import com.gestormei.data.database.AppDatabase
import com.gestormei.data.model.Cliente
import com.gestormei.data.model.Compromisso
import com.gestormei.data.model.Despesa
import com.gestormei.data.model.EmailRegistro
import com.gestormei.data.model.Empresa
import com.gestormei.data.model.Ideia
import com.gestormei.data.model.Projeto
import com.gestormei.data.model.Receita
import com.gestormei.data.model.Senha
import org.json.JSONArray
import org.json.JSONObject

/**
 * Backup e restauração de todos os dados em JSON (local).
 * As senhas são exportadas como estão no banco (cifradas) — por isso o cofre
 * só é totalmente restaurável no mesmo aparelho.
 */
class BackupManager(private val db: AppDatabase) {

    suspend fun exportarJson(): String {
        val raiz = JSONObject()
        raiz.put("versao", 4)
        raiz.put("empresas", JSONArray(db.empresaDao().todas().map { it.toJson() }))
        raiz.put("receitas", JSONArray(db.receitaDao().todas().map { it.toJson() }))
        raiz.put("despesas", JSONArray(db.despesaDao().todas().map { it.toJson() }))
        raiz.put("emails", JSONArray(db.emailDao().todas().map { it.toJson() }))
        raiz.put("senhas", JSONArray(db.senhaDao().todas().map { it.toJson() }))
        raiz.put("compromissos", JSONArray(db.compromissoDao().todas().map { it.toJson() }))
        raiz.put("projetos", JSONArray(db.projetoDao().todas().map { it.toJson() }))
        raiz.put("ideias", JSONArray(db.ideiaDao().todas().map { it.toJson() }))
        raiz.put("clientes", JSONArray(db.clienteDao().todas().map { it.toJson() }))
        return raiz.toString(2)
    }

    /** Substitui todos os dados pelos do backup. Retorna o total de registros. */
    suspend fun importarJson(json: String): Int {
        val raiz = JSONObject(json)
        db.clearAllTables()
        var total = 0
        raiz.optJSONArray("empresas")?.let { a ->
            for (i in 0 until a.length()) { db.empresaDao().inserir(empresaFrom(a.getJSONObject(i))); total++ }
        }
        raiz.optJSONArray("receitas")?.let { a ->
            for (i in 0 until a.length()) { db.receitaDao().inserir(receitaFrom(a.getJSONObject(i))); total++ }
        }
        raiz.optJSONArray("despesas")?.let { a ->
            for (i in 0 until a.length()) { db.despesaDao().inserir(despesaFrom(a.getJSONObject(i))); total++ }
        }
        raiz.optJSONArray("emails")?.let { a ->
            for (i in 0 until a.length()) { db.emailDao().inserir(emailFrom(a.getJSONObject(i))); total++ }
        }
        raiz.optJSONArray("senhas")?.let { a ->
            for (i in 0 until a.length()) { db.senhaDao().inserir(senhaFrom(a.getJSONObject(i))); total++ }
        }
        raiz.optJSONArray("compromissos")?.let { a ->
            for (i in 0 until a.length()) { db.compromissoDao().inserir(compromissoFrom(a.getJSONObject(i))); total++ }
        }
        raiz.optJSONArray("projetos")?.let { a ->
            for (i in 0 until a.length()) { db.projetoDao().inserir(projetoFrom(a.getJSONObject(i))); total++ }
        }
        raiz.optJSONArray("ideias")?.let { a ->
            for (i in 0 until a.length()) { db.ideiaDao().inserir(ideiaFrom(a.getJSONObject(i))); total++ }
        }
        raiz.optJSONArray("clientes")?.let { a ->
            for (i in 0 until a.length()) { db.clienteDao().inserir(clienteFrom(a.getJSONObject(i))); total++ }
        }
        return total
    }

    // ---- Serialização ----

    private fun Empresa.toJson() = JSONObject().apply {
        put("id", id); put("nomeFantasia", nomeFantasia); put("razaoSocial", razaoSocial)
        put("cnpj", cnpj); put("atividadePrincipal", atividadePrincipal); put("telefone", telefone)
        put("email", email); put("chavePix", chavePix); put("observacoes", observacoes)
        put("limiteAnual", limiteAnual); put("metaMensal", metaMensal)
    }

    private fun empresaFrom(o: JSONObject) = Empresa(
        id = o.getLong("id"), nomeFantasia = o.optString("nomeFantasia"),
        razaoSocial = o.optString("razaoSocial"), cnpj = o.optString("cnpj"),
        atividadePrincipal = o.optString("atividadePrincipal"), telefone = o.optString("telefone"),
        email = o.optString("email"), chavePix = o.optString("chavePix"),
        observacoes = o.optString("observacoes"), limiteAnual = o.optDouble("limiteAnual", 81000.0),
        metaMensal = o.optDouble("metaMensal", 0.0)
    )

    private fun Receita.toJson() = JSONObject().apply {
        put("id", id); put("empresaId", empresaId); put("data", data); put("cliente", cliente)
        put("descricao", descricao); put("valor", valor); put("formaPagamento", formaPagamento)
        put("numeroNota", numeroNota); put("origem", origem); put("referencia", referencia)
        put("contaNoLimite", contaNoLimite)
    }

    private fun receitaFrom(o: JSONObject) = Receita(
        id = o.getLong("id"), empresaId = o.getLong("empresaId"), data = o.optString("data"),
        cliente = o.optString("cliente"), descricao = o.optString("descricao"),
        valor = o.optDouble("valor", 0.0), formaPagamento = o.optString("formaPagamento"),
        numeroNota = o.optString("numeroNota"), origem = o.optString("origem"),
        referencia = o.optString("referencia"), contaNoLimite = o.optBoolean("contaNoLimite", true)
    )

    private fun Despesa.toJson() = JSONObject().apply {
        put("id", id); put("empresaId", empresaId); put("data", data); put("fornecedor", fornecedor)
        put("descricao", descricao); put("categoria", categoria); put("valor", valor)
        put("numeroNota", numeroNota); put("anexoUri", anexoUri); put("origem", origem)
        put("referencia", referencia)
    }

    private fun despesaFrom(o: JSONObject) = Despesa(
        id = o.getLong("id"), empresaId = o.getLong("empresaId"), data = o.optString("data"),
        fornecedor = o.optString("fornecedor"), descricao = o.optString("descricao"),
        categoria = o.optString("categoria"), valor = o.optDouble("valor", 0.0),
        numeroNota = o.optString("numeroNota"), anexoUri = o.optString("anexoUri"),
        origem = o.optString("origem"), referencia = o.optString("referencia")
    )

    private fun EmailRegistro.toJson() = JSONObject().apply {
        put("id", id); put("empresaId", empresaId); put("email", email); put("servico", servico)
        put("finalidade", finalidade); put("observacoes", observacoes)
    }

    private fun emailFrom(o: JSONObject) = EmailRegistro(
        id = o.getLong("id"), empresaId = o.getLong("empresaId"), email = o.optString("email"),
        servico = o.optString("servico"), finalidade = o.optString("finalidade"),
        observacoes = o.optString("observacoes")
    )

    private fun Senha.toJson() = JSONObject().apply {
        put("id", id); put("empresaId", empresaId); put("nomeServico", nomeServico); put("link", link)
        put("login", login); put("senha", senha); put("observacoes", observacoes)
    }

    private fun senhaFrom(o: JSONObject) = Senha(
        id = o.getLong("id"), empresaId = o.getLong("empresaId"), nomeServico = o.optString("nomeServico"),
        link = o.optString("link"), login = o.optString("login"), senha = o.optString("senha"),
        observacoes = o.optString("observacoes")
    )

    private fun Compromisso.toJson() = JSONObject().apply {
        put("id", id); put("empresaId", empresaId); put("titulo", titulo); put("data", data)
        put("hora", hora); put("cliente", cliente); put("descricao", descricao); put("status", status)
    }

    private fun compromissoFrom(o: JSONObject) = Compromisso(
        id = o.getLong("id"), empresaId = o.getLong("empresaId"), titulo = o.optString("titulo"),
        data = o.optString("data"), hora = o.optString("hora"), cliente = o.optString("cliente"),
        descricao = o.optString("descricao"), status = o.optString("status", "Pendente")
    )

    private fun Projeto.toJson() = JSONObject().apply {
        put("id", id); put("empresaId", empresaId); put("nome", nome); put("cliente", cliente)
        put("descricao", descricao); put("dataInicio", dataInicio); put("dataEntrega", dataEntrega)
        put("status", status); put("prioridade", prioridade); put("observacoes", observacoes)
    }

    private fun projetoFrom(o: JSONObject) = Projeto(
        id = o.getLong("id"), empresaId = o.getLong("empresaId"), nome = o.optString("nome"),
        cliente = o.optString("cliente"), descricao = o.optString("descricao"),
        dataInicio = o.optString("dataInicio"), dataEntrega = o.optString("dataEntrega"),
        status = o.optString("status", "Ideia"), prioridade = o.optString("prioridade", "Média"),
        observacoes = o.optString("observacoes")
    )

    private fun Ideia.toJson() = JSONObject().apply {
        put("id", id); put("empresaId", empresaId); put("titulo", titulo); put("descricao", descricao)
        put("categoria", categoria); put("prioridade", prioridade); put("status", status)
        put("dataCriacao", dataCriacao)
    }

    private fun ideiaFrom(o: JSONObject) = Ideia(
        id = o.getLong("id"), empresaId = o.getLong("empresaId"), titulo = o.optString("titulo"),
        descricao = o.optString("descricao"), categoria = o.optString("categoria"),
        prioridade = o.optString("prioridade", "Média"), status = o.optString("status", "Nova"),
        dataCriacao = o.optString("dataCriacao")
    )

    private fun Cliente.toJson() = JSONObject().apply {
        put("id", id); put("empresaId", empresaId); put("nome", nome); put("email", email)
        put("telefone", telefone); put("observacoes", observacoes)
    }

    private fun clienteFrom(o: JSONObject) = Cliente(
        id = o.getLong("id"), empresaId = o.getLong("empresaId"), nome = o.optString("nome"),
        email = o.optString("email"), telefone = o.optString("telefone"),
        observacoes = o.optString("observacoes")
    )
}
