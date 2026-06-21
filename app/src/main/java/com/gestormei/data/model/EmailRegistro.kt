package com.gestormei.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emails")
data class EmailRegistro(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val empresaId: Long,
    val email: String = "",
    val servico: String = "",
    val finalidade: String = "",
    val observacoes: String = ""
)
