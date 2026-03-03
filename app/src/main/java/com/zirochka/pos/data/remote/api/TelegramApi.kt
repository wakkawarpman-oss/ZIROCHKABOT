package com.zirochka.pos.data.remote.api

import com.zirochka.pos.data.remote.model.TelegramRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface TelegramApi {
    @POST("bot{token}/sendMessage")
    suspend fun sendMessage(
        @Path("token") token: String,
        @Body body: TelegramRequest
    ): Response<Unit>
}
