package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.models.RentalCategory
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
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

data class AiListingDraft(
    val objectTitle: String,
    val categoryKey: String,
    val description: String,
    val observedCondition: String,
    val suggestedDailyRateInr: Int,
    val recommendedDepositInr: Int,
    val compatibleAccessories: List<String>
) {
    fun toRentalCategory(fallback: RentalCategory = RentalCategory.ELECTRONICS): RentalCategory {
        return RentalCategory.entries.firstOrNull {
            it.name.equals(categoryKey.trim(), ignoreCase = true)
        } ?: fallback
    }

    fun toGearValuation(): AiGearValuation {
        return AiGearValuation(
            suggestedDailyRateInr = suggestedDailyRateInr,
            recommendedDepositInr = recommendedDepositInr,
            valuationSummary = buildString {
                if (observedCondition.isNotBlank()) {
                    append(observedCondition)
                    append(". ")
                }
                append(description)
            }.trim(),
            compatibleAccessories = compatibleAccessories
        )
    }
}

sealed interface AiValuationUiState {
    data object Idle : AiValuationUiState
    data object Loading : AiValuationUiState
    data class Success(val valuation: AiGearValuation) : AiValuationUiState
    data class Error(val message: String) : AiValuationUiState
}

sealed interface AiPhotoAutoFillUiState {
    data object Idle : AiPhotoAutoFillUiState
    data object Loading : AiPhotoAutoFillUiState
    data class Success(
        val draft: AiListingDraft,
        val appliedAllFields: Boolean
    ) : AiPhotoAutoFillUiState
    data class Error(val message: String) : AiPhotoAutoFillUiState
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

    private val listingDraftSchema = Schema.obj(
        properties = mapOf(
            "objectTitle" to Schema.string(
                description = "Concise 3 to 7 word marketplace item title identifying brand, model, or item type visible in the photos"
            ),
            "categoryKey" to Schema.enumeration(
                values = RentalCategory.entries.map { it.name },
                description = "Best matching Loop department enum key"
            ),
            "description" to Schema.string(
                description = "2 to 3 clear sentences describing the visible item, apparent physical condition, included parts seen in the photo, and pickup readiness"
            ),
            "observedCondition" to Schema.string(
                description = "Short 4 to 8 word observed condition summary from the photo"
            ),
            "suggestedDailyRateInr" to Schema.integer(
                description = "Fair daily community rental rate in Indian Rupees (INR), integer only"
            ),
            "recommendedDepositInr" to Schema.integer(
                description = "Recommended refundable security deposit in Indian Rupees (INR), integer only"
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

    private val photoListingModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = listingDraftSchema
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

    suspend fun analyzePhotosForListing(
        photos: List<Bitmap>,
        existingTitle: String = "",
        existingNotes: String = ""
    ): Result<AiListingDraft> {
        if (photos.isEmpty()) {
            return Result.failure(IllegalArgumentException("Attach at least one photo to analyze."))
        }

        val samplePhotos = photos.take(3)
        val promptText = buildString {
            appendLine("You are a gear and item curator for Loop, an Indian peer-to-peer neighborhood sharing marketplace.")
            appendLine("Inspect the attached photo(s) carefully and generate an accurate, honest listing draft.")
            appendLine("Choose categoryKey strictly from: ${RentalCategory.entries.joinToString(", ") { it.name }}.")
            appendLine("Price suggestedDailyRateInr and recommendedDepositInr fairly in Indian Rupees (INR) for local peer-to-peer rentals.")
            appendLine("Describe only features, accessories, and condition details that are genuinely visible in the photo(s).")
            if (existingTitle.isNotBlank()) {
                appendLine("Host's current draft title for context: $existingTitle")
            }
            if (existingNotes.isNotBlank()) {
                appendLine("Host's current draft notes for context: $existingNotes")
            }
        }

        return try {
            val multimodalPrompt = content {
                text(promptText)
                samplePhotos.forEach { bitmap ->
                    image(bitmap)
                }
            }

            val response = withTimeout(60_000L) {
                photoListingModel.generateContent(multimodalPrompt)
            }
            val rawJson = response.text?.takeIf { it.isNotBlank() }
                ?: return Result.failure(IllegalStateException("Gemini returned an empty response."))

            val json = JSONObject(rawJson)
            val objectTitle = json.optString("objectTitle", "").trim()
                .ifBlank { existingTitle.ifBlank { "Community Gear Listing" } }
            val categoryKey = json.optString("categoryKey", RentalCategory.ELECTRONICS.name).trim()
            val description = json.optString("description", "").trim()
                .ifBlank { "Available for local neighborhood rental. Ready for pickup." }
            val observedCondition = json.optString("observedCondition", "").trim()
                .ifBlank { "Good visible condition" }
            val dailyRate = json.optInt("suggestedDailyRateInr", 0).coerceAtLeast(50)
            val deposit = json.optInt("recommendedDepositInr", 0).coerceAtLeast(200)
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
                AiListingDraft(
                    objectTitle = objectTitle,
                    categoryKey = categoryKey,
                    description = description,
                    observedCondition = observedCondition,
                    suggestedDailyRateInr = dailyRate,
                    recommendedDepositInr = deposit,
                    compatibleAccessories = accessories
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini photo analysis timed out", e)
            Result.failure(Exception("Photo analysis timed out after 60 seconds. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini photo analysis failed", e)
            Result.failure(Exception(e.localizedMessage ?: "Could not analyze attached photos."))
        }
    }
}
