package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@Serializable
data class Candidate(
    val content: ContentResponse
)

@Serializable
data class ContentResponse(
    val parts: List<PartResponse>
)

@Serializable
data class PartResponse(
    val text: String? = null
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiAiClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiApi::class.java)
    }
}

object GeminiAiService {
    suspend fun evaluateGearAndSuggestPricing(gearTitle: String, description: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty()) {
            return@withContext "Estimated Daily Rate: $45.00 / day. Recommended Deposit: $200. Great choice for creators!"
        }
        val prompt = "Act as an expert gear appraiser for a high-end creator marketplace called Loop. Analyze this gear listing: Title: '$gearTitle', Description: '$description'. Provide a concise valuation with suggested fair daily rental price in USD, recommended security deposit, and 3 matching accessory recommendations."
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )
        try {
            val resp = GeminiAiClient.api.generateContent(apiKey, request)
            resp.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Could not evaluate gear."
        } catch (e: Exception) {
            "AI Valuation unavailable: ${e.localizedMessage}. Suggested rate: $50/day."
        }
    }
}
