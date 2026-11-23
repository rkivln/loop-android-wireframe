package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.models.RentalCategory
import com.example.ui.components.PhotoBitmapProcessor
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.appcheck.FirebaseAppCheck
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
    data class Loading(
        val statusMessage: String = "Appraising piece with Gemini AI..."
    ) : AiValuationUiState
    data class Success(val valuation: AiGearValuation) : AiValuationUiState
    data class Error(val message: String) : AiValuationUiState
}

sealed interface AiPhotoAutoFillUiState {
    data object Idle : AiPhotoAutoFillUiState
    data class Loading(
        val statusMessage: String = "Inspecting gear photos with Gemini..."
    ) : AiPhotoAutoFillUiState
    data class Success(
        val draft: AiListingDraft,
        val appliedAllFields: Boolean,
        val modelUsed: String = GeminiAiService.PRIMARY_MODEL
    ) : AiPhotoAutoFillUiState
    data class Error(val message: String) : AiPhotoAutoFillUiState
}

data class AiHouseDraft(
    val title: String,
    val propertyTypeKey: String,
    val furnishingKey: String,
    val estimatedBhk: Int,
    val estimatedAreaSqFt: Int,
    val description: String,
    val suggestedMonthlyRentInr: Int,
    val recommendedDepositInr: Int,
    val photoRoomLabels: List<String>
)

sealed interface AiHouseAutoFillUiState {
    data object Idle : AiHouseAutoFillUiState
    data class Loading(
        val statusMessage: String = "Inspecting property rooms with Gemini..."
    ) : AiHouseAutoFillUiState
    data class Success(
        val draft: AiHouseDraft,
        val modelUsed: String = GeminiAiService.PRIMARY_MODEL
    ) : AiHouseAutoFillUiState
    data class Error(val message: String) : AiHouseAutoFillUiState
}

object GeminiAiService {
    private const val TAG = "GeminiAiService"

    const val PRIMARY_MODEL = "gemini-3.8-flash"
    const val LITE_FALLBACK_MODEL = "gemini-3.5-flash-lite"

    private const val PHOTO_PRIMARY_TIMEOUT_MS = 25_000L
    private const val PHOTO_FALLBACK_TIMEOUT_MS = 30_000L
    private const val VALUATION_PRIMARY_TIMEOUT_MS = 20_000L
    private const val VALUATION_FALLBACK_TIMEOUT_MS = 25_000L

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

    private val valuationGenerationConfig = generationConfig {
        responseMimeType = "application/json"
        responseSchema = valuationSchema
    }

    private val listingGenerationConfig = generationConfig {
        responseMimeType = "application/json"
        responseSchema = listingDraftSchema
    }

    private val primaryValuationModel by lazy {
        Firebase.ai.generativeModel(
            modelName = PRIMARY_MODEL,
            generationConfig = valuationGenerationConfig
        )
    }

    private val liteValuationModel by lazy {
        Firebase.ai.generativeModel(
            modelName = LITE_FALLBACK_MODEL,
            generationConfig = valuationGenerationConfig
        )
    }

    private val primaryPhotoListingModel by lazy {
        Firebase.ai.generativeModel(
            modelName = PRIMARY_MODEL,
            generationConfig = listingGenerationConfig
        )
    }

    private val litePhotoListingModel by lazy {
        Firebase.ai.generativeModel(
            modelName = LITE_FALLBACK_MODEL,
            generationConfig = listingGenerationConfig
        )
    }

    private val houseDraftSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(
                description = "Warm editorial 4 to 8 word house rental headline (e.g. 'Sunlit 2 BHK Courtyard Flat')"
            ),
            "propertyTypeKey" to Schema.enumeration(
                values = listOf("APARTMENT", "INDEPENDENT_HOUSE", "VILLA", "STUDIO", "SHARED_ROOM"),
                description = "Matching residential property type key"
            ),
            "furnishingKey" to Schema.enumeration(
                values = listOf("UNFURNISHED", "SEMI_FURNISHED", "FULLY_FURNISHED"),
                description = "Observed furnishing state from attached room photos"
            ),
            "estimatedBhk" to Schema.integer(
                description = "Estimated bedroom count (0 for Studio, 1-5 otherwise)"
            ),
            "estimatedAreaSqFt" to Schema.integer(
                description = "Estimated interior area in square feet"
            ),
            "description" to Schema.string(
                description = "2 to 3 honest sentences describing light, room layout, furnishing, and neighborhood livability visible in the photos"
            ),
            "suggestedMonthlyRentInr" to Schema.integer(
                description = "Realistic long-term monthly rent in Indian Rupees (INR) for Puducherry / Indian coastal neighborhoods"
            ),
            "recommendedDepositInr" to Schema.integer(
                description = "Recommended refundable security deposit in INR (typically 3-5x monthly rent)"
            ),
            "photoRoomLabels" to Schema.array(
                items = Schema.string(),
                description = "Short room label for each attached photo (e.g. Living Room, Bedroom, Kitchen, Exterior)"
            )
        )
    )

    private val houseGenerationConfig = generationConfig {
        responseMimeType = "application/json"
        responseSchema = houseDraftSchema
    }

    private val primaryHouseModel by lazy {
        Firebase.ai.generativeModel(
            modelName = PRIMARY_MODEL,
            generationConfig = houseGenerationConfig
        )
    }

    private val liteHouseModel by lazy {
        Firebase.ai.generativeModel(
            modelName = LITE_FALLBACK_MODEL,
            generationConfig = houseGenerationConfig
        )
    }

    internal fun isTokenOrAppCheckError(error: Throwable): Boolean {
        var current: Throwable? = error
        while (current != null) {
            val msg = current.message.orEmpty().lowercase()
            if (
                msg.contains("app check") ||
                msg.contains("appcheck") ||
                msg.contains("attestation") ||
                msg.contains("firebase token") ||
                msg.contains("token error") ||
                msg.contains("403") ||
                msg.contains("unauthenticated") ||
                msg.contains("permission_denied")
            ) {
                return true
            }
            current = current.cause
        }
        return false
    }

    internal fun formatFriendlyError(error: Throwable, isPhoto: Boolean): String {
        if (error is TimeoutCancellationException) {
            return if (isPhoto) {
                "Photo analysis timed out on both primary and Gemini Lite models. Check your network connection and try again."
            } else {
                "AI appraisal timed out on both primary and Gemini Lite models. Check your network connection and try again."
            }
        }
        if (isTokenOrAppCheckError(error)) {
            return "Firebase App Check token error. Ensure network & automatic device time are enabled, then tap Retry."
        }
        val rawMsg = error.localizedMessage.orEmpty()
        if (rawMsg.contains("429") || rawMsg.contains("quota", ignoreCase = true) || rawMsg.contains("resource_exhausted", ignoreCase = true)) {
            return "Gemini service is temporarily busy across models. Please tap Retry in a moment."
        }
        return rawMsg.ifBlank {
            if (isPhoto) "Could not analyze attached photos." else "Gemini AI appraisal failed."
        }
    }

    private suspend fun refreshAppCheckTokenBestEffort() {
        try {
            FirebaseAppCheck.getInstance().getAppCheckToken(true).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Best-effort App Check token refresh failed: ${e.message}", e)
        }
    }

    internal suspend fun <T> executeWithLiteFallback(
        primaryTimeoutMs: Long,
        fallbackTimeoutMs: Long,
        onStatusUpdate: suspend (String) -> Unit,
        onRefreshToken: suspend () -> Unit,
        primaryCall: suspend () -> T,
        fallbackCall: suspend () -> T
    ): Pair<T, String> {
        return try {
            val primaryResult = withTimeout(primaryTimeoutMs) {
                primaryCall()
            }
            primaryResult to PRIMARY_MODEL
        } catch (timeout: TimeoutCancellationException) {
            Log.w(TAG, "Primary model ($PRIMARY_MODEL) timed out after ${primaryTimeoutMs}ms; falling back to $LITE_FALLBACK_MODEL", timeout)
            onStatusUpdate("Primary model timed out — switching to Gemini Lite...")
            val fallbackResult = withTimeout(fallbackTimeoutMs) {
                fallbackCall()
            }
            fallbackResult to LITE_FALLBACK_MODEL
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (primaryError: Exception) {
            Log.w(TAG, "Primary model ($PRIMARY_MODEL) failed (${primaryError.message}); falling back to $LITE_FALLBACK_MODEL", primaryError)
            if (isTokenOrAppCheckError(primaryError)) {
                onStatusUpdate("Refreshing Firebase verification & retrying with Gemini Lite...")
                onRefreshToken()
            } else {
                onStatusUpdate("Switching to fast Gemini Lite model...")
            }
            val fallbackResult = withTimeout(fallbackTimeoutMs) {
                fallbackCall()
            }
            fallbackResult to LITE_FALLBACK_MODEL
        }
    }

    internal fun parseValuationJson(rawJson: String): AiGearValuation {
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
        return AiGearValuation(
            suggestedDailyRateInr = dailyRate,
            recommendedDepositInr = deposit,
            valuationSummary = summary,
            compatibleAccessories = accessories
        )
    }

    internal fun parseListingDraftJson(rawJson: String, existingTitle: String = ""): AiListingDraft {
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
        return AiListingDraft(
            objectTitle = objectTitle,
            categoryKey = categoryKey,
            description = description,
            observedCondition = observedCondition,
            suggestedDailyRateInr = dailyRate,
            recommendedDepositInr = deposit,
            compatibleAccessories = accessories
        )
    }

    suspend fun evaluateGearAndSuggestPricing(
        gearTitle: String,
        description: String,
        onStatusUpdate: (String) -> Unit = {}
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
            onStatusUpdate("Appraising piece with Gemini...")
            val (rawJson, _) = withContext(Dispatchers.IO) {
                executeWithLiteFallback(
                    primaryTimeoutMs = VALUATION_PRIMARY_TIMEOUT_MS,
                    fallbackTimeoutMs = VALUATION_FALLBACK_TIMEOUT_MS,
                    onStatusUpdate = { status ->
                        withContext(Dispatchers.Main) { onStatusUpdate(status) }
                    },
                    onRefreshToken = { refreshAppCheckTokenBestEffort() },
                    primaryCall = {
                        val response = primaryValuationModel.generateContent(prompt)
                        response.text?.takeIf { it.isNotBlank() }
                            ?: throw IllegalStateException("Gemini returned an empty response.")
                    },
                    fallbackCall = {
                        val response = liteValuationModel.generateContent(prompt)
                        response.text?.takeIf { it.isNotBlank() }
                            ?: throw IllegalStateException("Gemini Lite returned an empty response.")
                    }
                )
            }

            Result.success(parseValuationJson(rawJson))
        } catch (t: TimeoutCancellationException) {
            Log.e(TAG, "Gemini valuation timed out across primary and lite models", t)
            Result.failure(Exception(formatFriendlyError(t, isPhoto = false), t))
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            Log.e(TAG, "Gemini valuation failed", e)
            Result.failure(Exception(formatFriendlyError(e, isPhoto = false), e))
        }
    }

    suspend fun analyzePhotosForListing(
        photos: List<Bitmap>,
        existingTitle: String = "",
        existingNotes: String = "",
        onStatusUpdate: (String) -> Unit = {}
    ): Result<AiListingDraft> {
        if (photos.isEmpty()) {
            return Result.failure(IllegalArgumentException("Attach at least one photo to analyze."))
        }

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
            onStatusUpdate("Optimizing photo & scanning with Gemini...")
            val (rawJson, _) = withContext(Dispatchers.IO) {
                val primaryPhotos = photos.take(2).map { bmp ->
                    PhotoBitmapProcessor.prepareBitmapForAi(bmp, maxEdge = 640)
                }
                val fallbackCoverPhoto by lazy {
                    PhotoBitmapProcessor.prepareBitmapForAi(photos.first(), maxEdge = 512)
                }

                executeWithLiteFallback(
                    primaryTimeoutMs = PHOTO_PRIMARY_TIMEOUT_MS,
                    fallbackTimeoutMs = PHOTO_FALLBACK_TIMEOUT_MS,
                    onStatusUpdate = { status ->
                        withContext(Dispatchers.Main) { onStatusUpdate(status) }
                    },
                    onRefreshToken = { refreshAppCheckTokenBestEffort() },
                    primaryCall = {
                        val multimodalPrompt = content {
                            text(promptText)
                            primaryPhotos.forEach { bitmap ->
                                image(bitmap)
                            }
                        }
                        val response = primaryPhotoListingModel.generateContent(multimodalPrompt)
                        response.text?.takeIf { it.isNotBlank() }
                            ?: throw IllegalStateException("Gemini returned an empty response.")
                    },
                    fallbackCall = {
                        val litePrompt = content {
                            text(promptText)
                            image(fallbackCoverPhoto)
                        }
                        val response = litePhotoListingModel.generateContent(litePrompt)
                        response.text?.takeIf { it.isNotBlank() }
                            ?: throw IllegalStateException("Gemini Lite returned an empty response.")
                    }
                )
            }

            Result.success(parseListingDraftJson(rawJson, existingTitle))
        } catch (t: TimeoutCancellationException) {
            Log.e(TAG, "Gemini photo analysis timed out across primary and lite models", t)
            Result.failure(Exception(formatFriendlyError(t, isPhoto = true), t))
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            Log.e(TAG, "Gemini photo analysis failed", e)
            Result.failure(Exception(formatFriendlyError(e, isPhoto = true), e))
        }
    }

    suspend fun analyzeHousePhotosForListing(
        photos: List<Bitmap>,
        existingTitle: String = "",
        neighborhoodHint: String = "",
        onStatusUpdate: (String) -> Unit = {}
    ): Result<AiHouseDraft> {
        if (photos.isEmpty()) {
            return Result.failure(IllegalArgumentException("Attach at least one property photo to scan."))
        }

        val promptText = buildString {
            appendLine("You are a residential rental curator for Loop, a Puducherry & Indian neighborhood housing community.")
            appendLine("Inspect the attached room/exterior photos carefully and create an honest long-term monthly rental draft.")
            appendLine("Do not invent amenities not supported by the photos.")
            if (existingTitle.isNotBlank()) {
                appendLine("Landlord's title note: $existingTitle")
            }
            if (neighborhoodHint.isNotBlank()) {
                appendLine("Neighborhood: $neighborhoodHint")
            }
        }

        return try {
            onStatusUpdate("Analyzing rooms & monthly rent with Gemini...")
            val (rawJson, _) = withContext(Dispatchers.IO) {
                val preparedPhotos = photos.take(4).map { bmp ->
                    PhotoBitmapProcessor.prepareBitmapForAi(bmp, maxEdge = 640)
                }
                val fallbackCover by lazy {
                    PhotoBitmapProcessor.prepareBitmapForAi(photos.first(), maxEdge = 512)
                }

                executeWithLiteFallback(
                    primaryTimeoutMs = PHOTO_PRIMARY_TIMEOUT_MS,
                    fallbackTimeoutMs = PHOTO_FALLBACK_TIMEOUT_MS,
                    onStatusUpdate = { status ->
                        withContext(Dispatchers.Main) { onStatusUpdate(status) }
                    },
                    onRefreshToken = { refreshAppCheckTokenBestEffort() },
                    primaryCall = {
                        val multimodal = content {
                            text(promptText)
                            preparedPhotos.forEach { image(it) }
                        }
                        val response = primaryHouseModel.generateContent(multimodal)
                        response.text?.takeIf { it.isNotBlank() }
                            ?: throw IllegalStateException("Gemini returned an empty response.")
                    },
                    fallbackCall = {
                        val litePrompt = content {
                            text(promptText)
                            image(fallbackCover)
                        }
                        val response = liteHouseModel.generateContent(litePrompt)
                        response.text?.takeIf { it.isNotBlank() }
                            ?: throw IllegalStateException("Gemini Lite returned an empty response.")
                    }
                )
            }

            val json = JSONObject(rawJson)
            val labelsJson = json.optJSONArray("photoRoomLabels")
            val labels = buildList {
                if (labelsJson != null) {
                    for (i in 0 until labelsJson.length()) {
                        val lbl = labelsJson.optString(i).trim()
                        if (lbl.isNotBlank()) add(lbl.take(30))
                    }
                }
            }
            val rent = json.optInt("suggestedMonthlyRentInr", 18000).coerceIn(3000, 300000)
            val deposit = json.optInt("recommendedDepositInr", rent * 3).coerceIn(0, 1500000)
            Result.success(
                AiHouseDraft(
                    title = json.optString("title", "").trim().ifBlank {
                        existingTitle.ifBlank { "Sunlit Neighborhood Home" }
                    },
                    propertyTypeKey = json.optString("propertyTypeKey", "APARTMENT").trim(),
                    furnishingKey = json.optString("furnishingKey", "SEMI_FURNISHED").trim(),
                    estimatedBhk = json.optInt("estimatedBhk", 2).coerceIn(0, 8),
                    estimatedAreaSqFt = json.optInt("estimatedAreaSqFt", 950).coerceIn(200, 10000),
                    description = json.optString("description", "").trim().ifBlank {
                        "Bright long-term rental ready for scheduled walkthrough visits."
                    },
                    suggestedMonthlyRentInr = rent,
                    recommendedDepositInr = deposit,
                    photoRoomLabels = labels
                )
            )
        } catch (t: TimeoutCancellationException) {
            Log.e(TAG, "Gemini house photo analysis timed out", t)
            Result.failure(Exception(formatFriendlyError(t, isPhoto = true), t))
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            Log.e(TAG, "Gemini house photo analysis failed", e)
            Result.failure(Exception(formatFriendlyError(e, isPhoto = true), e))
        }
    }
}
