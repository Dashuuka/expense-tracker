package com.expensetracker.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// ---- Models ----

data class ExchangeRate(
    @SerializedName("Cur_ID") val id: Int,
    @SerializedName("Date") val date: String,
    @SerializedName("Cur_Abbreviation") val abbreviation: String,
    @SerializedName("Cur_Scale") val scale: Int,
    @SerializedName("Cur_Name") val name: String,
    @SerializedName("Cur_OfficialRate") val officialRate: Double
)

data class Currency(
    @SerializedName("Cur_ID") val id: Int,
    @SerializedName("Cur_Abbreviation") val abbreviation: String,
    @SerializedName("Cur_Name") val name: String,
    @SerializedName("Cur_Scale") val scale: Int
)

// ---- API Interface ----

interface NbrbApi {

    /**
     * Get all exchange rates for a given date.
     * ondate format: "2025-01-01", if null → today
     * periodicity: 0 = daily, 1 = monthly
     */
    @GET("exrates/rates")
    suspend fun getRates(
        @Query("ondate") onDate: String? = null,
        @Query("periodicity") periodicity: Int = 0
    ): List<ExchangeRate>

    /**
     * Get single rate by currency abbreviation (e.g. "USD", "EUR", "RUB")
     */
    @GET("exrates/rates/{abbreviation}")
    suspend fun getRateByAbbreviation(
        @Path("abbreviation") abbreviation: String,
        @Query("parammode") paramMode: Int = 2,
        @Query("ondate") onDate: String? = null
    ): ExchangeRate
}
