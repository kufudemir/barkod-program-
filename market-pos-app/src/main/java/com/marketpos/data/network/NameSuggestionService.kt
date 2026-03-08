package com.marketpos.data.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface NameSuggestionService {
    @GET
    suspend fun rawGet(@Url url: String): ResponseBody
}
