package com.gestormei.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projetos")
data class Projeto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val empresaId: Long,
    val nome: String = "",
    val cliente: String = "",
    val descricao: String = "",
    val dataInicio: String = "",
    val dataEntrega: String = "",
    val status: String = "Ideia",
    val prioridade: String = "Média",
    val observacoes: String = ""
)
