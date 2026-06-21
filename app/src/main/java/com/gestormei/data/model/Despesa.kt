package com.gestormei.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "despesas")
data class Despesa(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val empresaId: Long,
    val data: String = "",
    val fornecedor: String = "",
    val descricao: String = "",
    val categoria: String = "",
    val valor: Double = 0.0,
    val numeroNota: String = ""
)
