package com.zirochka.pos.data.remote.api

import com.zirochka.pos.data.remote.model.SheetsRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GoogleSheetsApi {
    @POST("exec")
    suspend fun appendBooking(@Body request: SheetsRequest): Response<Unit>
}
