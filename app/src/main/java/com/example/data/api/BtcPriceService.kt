package com.example.data.api

import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

@JsonClass(generateAdapter = true)
data class PriceInfo(
    val last: Double,
    val buy: Double,
    val sell: Double,
    val symbol: String
)

@JsonClass(generateAdapter = true)
data class LatestBlockResponse(
    val hash: String,
    val time: Long,
    val block_index: Long,
    val height: Long
)

interface BtcPriceService {
    @GET("ticker")
    suspend fun getTicker(): Map<String, PriceInfo>

    @GET("latestblock")
    suspend fun getLatestBlock(): LatestBlockResponse
}

object BtcPriceRetrofitClient {
    private const val BASE_URL = "https://blockchain.info/"

    val service: BtcPriceService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(BtcPriceService::class.java)
        }
}

