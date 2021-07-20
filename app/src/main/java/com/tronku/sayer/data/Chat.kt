package com.tronku.sayer.data

import java.sql.Timestamp

data class Chat(
    val message: String,
    val direction: ChatDirection
)

enum class ChatDirection {
    INCOMING,
    OUTGOING
}
