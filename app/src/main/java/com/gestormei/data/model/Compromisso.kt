package com.gestormei.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "compromissos")
data class Compromisso(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val empresaId: Long,
    val titulo: String = "",
    val data: String = "",
    val hora: String = "",
    val cliente: String = "",
    val descricao: String = "",
    val status: String = "Pendente"
)
