package com.gestormei.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val empresaId: Long,
    val nome: String = "",
    val email: String = "",
    val telefone: String = "",
    val observacoes: String = ""
)
