package com.zirochka.pos.data.remote.model

data class TelegramRequest(
    val chat_id: String,
    val text: String,
    val parse_mode: String = "Markdown"
)
