package com.gestormei.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receitas")
data class Receita(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val empresaId: Long,
    val data: String = "",
    val cliente: String = "",
    val descricao: String = "",
    val valor: Double = 0.0,
    val formaPagamento: String = "",
    val numeroNota: String = "",
    val origem: String = "",
    val referencia: String = "",
    @ColumnInfo(defaultValue = "1") val contaNoLimite: Boolean = true
)
