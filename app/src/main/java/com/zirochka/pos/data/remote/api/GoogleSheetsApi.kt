package com.zirochka.pos.data.remote.api

import com.zirochka.pos.data.remote.model.SheetsRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface GoogleSheetsApi {
    @POST
    suspend fun appendBooking(
        @Url url: String,
        @Body request: SheetsRequest
    ): Response<Unit>
}
