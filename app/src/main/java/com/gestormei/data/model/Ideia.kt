package com.gestormei.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ideias")
data class Ideia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val empresaId: Long,
    val titulo: String = "",
    val descricao: String = "",
    val categoria: String = "",
    val prioridade: String = "Média",
    val status: String = "Nova",
    val dataCriacao: String = ""
)
