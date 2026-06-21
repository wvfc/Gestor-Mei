package com.gestormei.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "senhas")
data class Senha(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val empresaId: Long,
    val nomeServico: String = "",
    val link: String = "",
    val login: String = "",
    val senha: String = "",
    val observacoes: String = ""
)
