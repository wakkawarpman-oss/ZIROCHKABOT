package com.zirochka.pos.data.remote.model

data class SheetsRequest(
    val username: String,
    val message: String,
    val orderSummary: String? = null,
    val total: Double? = null
)
