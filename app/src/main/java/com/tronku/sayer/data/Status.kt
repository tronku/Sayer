package com.tronku.sayer.ui

data class Status(
        val timestamp: Long,
        val status: String,
        val type: Type
)

enum class Type {
    RECEIVED,
    SYNCED
}
