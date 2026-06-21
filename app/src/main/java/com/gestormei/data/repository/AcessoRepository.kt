package com.gestormei.data.repository

import com.gestormei.data.Cripto
import com.gestormei.data.dao.EmailDao
import com.gestormei.data.dao.SenhaDao
import com.gestormei.data.model.EmailRegistro
import com.gestormei.data.model.Senha
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AcessoRepository(
    private val senhaDao: SenhaDao,
    private val emailDao: EmailDao
) {

    fun observarSenhas(empresaId: Long): Flow<List<Senha>> =
        senhaDao.observarPorEmpresa(empresaId)
            .map { lista -> lista.map { it.copy(senha = Cripto.decifrar(it.senha)) } }

    fun observarEmails(empresaId: Long): Flow<List<EmailRegistro>> =
        emailDao.observarPorEmpresa(empresaId)

    suspend fun salvarSenha(senha: Senha) {
        val protegida = senha.copy(senha = Cripto.cifrar(senha.senha))
        if (protegida.id == 0L) senhaDao.inserir(protegida) else senhaDao.atualizar(protegida)
    }

    suspend fun excluirSenha(senha: Senha) = senhaDao.excluir(senha)

    suspend fun salvarEmail(email: EmailRegistro) {
        if (email.id == 0L) emailDao.inserir(email) else emailDao.atualizar(email)
    }

    suspend fun excluirEmail(email: EmailRegistro) = emailDao.excluir(email)
}
