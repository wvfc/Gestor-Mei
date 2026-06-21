package com.gestormei.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "empresas")
data class Empresa(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nomeFantasia: String,
    val razaoSocial: String = "",
    val cnpj: String = "",
    val atividadePrincipal: String = "",
    val telefone: String = "",
    val email: String = "",
    val chavePix: String = "",
    val observacoes: String = "",
    val limiteAnual: Double = 81000.0
)
