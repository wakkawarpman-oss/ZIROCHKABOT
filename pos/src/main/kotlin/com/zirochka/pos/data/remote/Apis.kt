package com.zirochka.pos.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Google Sheets append endpoint.
 * Base URL: https://sheets.googleapis.com/v4/spreadsheets/{SPREADSHEET_ID}/
 * Query params:
 * - valueInputOption: RAW (default) or USER_ENTERED
 * - insertDataOption: INSERT_ROWS (default)
 */
interface GoogleSheetsService {
    @POST("values:append")
    suspend fun appendOrder(
        @Body payload: SheetsAppendRequest,
        @Query("valueInputOption") valueInputOption: String = "RAW",
        @Query("insertDataOption") insertDataOption: String = "INSERT_ROWS"
    ): SheetsAppendResponse
}

interface TelegramService {
    @POST("sendMessage")
    suspend fun sendMessage(@Body request: TelegramMessageRequest): TelegramMessageResponse
}

@JsonClass(generateAdapter = true)
data class SheetsAppendRequest(
    @Json(name = "range") val range: String,
    @Json(name = "majorDimension") val majorDimension: String = "ROWS",
    @Json(name = "values") val values: List<List<Any>>
)

@JsonClass(generateAdapter = true)
data class SheetsAppendResponse(
    @Json(name = "spreadsheetId") val spreadsheetId: String? = null,
    @Json(name = "tableRange") val tableRange: String? = null
)

@JsonClass(generateAdapter = true)
data class TelegramMessageRequest(
    @Json(name = "chat_id") val chatId: String,
    @Json(name = "text") val text: String,
    @Json(name = "parse_mode") val parseMode: String = "MarkdownV2"
)

@JsonClass(generateAdapter = true)
data class TelegramMessageResponse(
    @Json(name = "ok") val ok: Boolean,
    @Json(name = "result") val result: Any? = null
)
