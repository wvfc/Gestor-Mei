package com.gestormei.data.repository

import com.gestormei.data.dao.EmailDao
import com.gestormei.data.dao.SenhaDao
import com.gestormei.data.model.EmailRegistro
import com.gestormei.data.model.Senha
import kotlinx.coroutines.flow.Flow

class AcessoRepository(
    private val senhaDao: SenhaDao,
    private val emailDao: EmailDao
) {

    fun observarSenhas(empresaId: Long): Flow<List<Senha>> =
        senhaDao.observarPorEmpresa(empresaId)

    fun observarEmails(empresaId: Long): Flow<List<EmailRegistro>> =
        emailDao.observarPorEmpresa(empresaId)

    suspend fun salvarSenha(senha: Senha) {
        if (senha.id == 0L) senhaDao.inserir(senha) else senhaDao.atualizar(senha)
    }

    suspend fun excluirSenha(senha: Senha) = senhaDao.excluir(senha)

    suspend fun salvarEmail(email: EmailRegistro) {
        if (email.id == 0L) emailDao.inserir(email) else emailDao.atualizar(email)
    }

    suspend fun excluirEmail(email: EmailRegistro) = emailDao.excluir(email)
}
