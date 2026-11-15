package com.example.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

data class AiGearValuation(
    val suggestedDailyRateInr: Int,
    val recommendedDepositInr: Int,
    val valuationSummary: String,
    val compatibleAccessories: List<String>
) {
    fun toFormattedText(): String {
        val accessoriesText = if (compatibleAccessories.isNotEmpty()) {
            "\n\nRecommended Accessories:\n" + compatibleAccessories.joinToString("\n") { "• $it" }
        } else {
            ""
        }
        return "Suggested Rate: ₹$suggestedDailyRateInr / day\n" +
            "Recommended Deposit: ₹$recommendedDepositInr\n\n" +
            valuationSummary +
            accessoriesText
    }
}

sealed interface AiValuationUiState {
    data object Idle : AiValuationUiState
    data object Loading : AiValuationUiState
    data class Success(val valuation: AiGearValuation) : AiValuationUiState
    data class Error(val message: String) : AiValuationUiState
}

object GeminiAiService {
    private const val TAG = "GeminiAiService"

    private val valuationSchema = Schema.obj(
        properties = mapOf(
            "suggestedDailyRateInr" to Schema.integer(
                description = "Recommended daily rental rate in Indian Rupees (INR), integer only"
            ),
            "recommendedDepositInr" to Schema.integer(
                description = "Recommended refundable security deposit in Indian Rupees (INR), integer only"
            ),
            "valuationSummary" to Schema.string(
                description = "Concise 2-sentence appraisal of the gear and rental demand in a coastal creative community"
            ),
            "compatibleAccessories" to Schema.array(
                items = Schema.string(),
                description = "Exactly 3 practical compatible accessories or add-ons for this item"
            )
        )
    )

    private val valuationModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = valuationSchema
            }
        )
    }

    suspend fun evaluateGearAndSuggestPricing(
        gearTitle: String,
        description: String
    ): Result<AiGearValuation> {
        if (gearTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("Enter a gear title first to evaluate."))
        }

        val prompt = buildString {
            appendLine("You are an equipment and item appraiser for Loop, a peer-to-peer community sharing marketplace.")
            appendLine("Evaluate this listing and recommend fair pricing in Indian Rupees (INR):")
            appendLine("Title: $gearTitle")
            appendLine("Description: ${description.ifBlank { "Standard user condition" }}")
        }

        return try {
            val response = withTimeout(60_000L) {
                valuationModel.generateContent(prompt)
            }
            val rawJson = response.text?.takeIf { it.isNotBlank() }
                ?: return Result.failure(IllegalStateException("Gemini returned an empty response."))

            val json = JSONObject(rawJson)
            val dailyRate = json.optInt("suggestedDailyRateInr", 0).coerceAtLeast(50)
            val deposit = json.optInt("recommendedDepositInr", 0).coerceAtLeast(200)
            val summary = json.optString("valuationSummary", "").ifBlank {
                "Fair daily rate calibrated for local community sharing."
            }
            val accJson = json.optJSONArray("compatibleAccessories")
            val accessories = buildList {
                if (accJson != null) {
                    for (i in 0 until accJson.length()) {
                        val item = accJson.optString(i)
                        if (item.isNotBlank()) add(item)
                    }
                }
            }

            Result.success(
                AiGearValuation(
                    suggestedDailyRateInr = dailyRate,
                    recommendedDepositInr = deposit,
                    valuationSummary = summary,
                    compatibleAccessories = accessories
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini valuation timed out", e)
            Result.failure(Exception("AI appraisal timed out after 60 seconds. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini valuation failed", e)
            Result.failure(Exception(e.localizedMessage ?: "Gemini AI appraisal failed."))
        }
    }
}
