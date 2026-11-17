package com.example.ai

import com.example.data.models.RentalCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GeminiAiServiceTest {

    @Test
    fun executeWithLiteFallback_returnsPrimaryWhenFast() = runBlocking {
        var tokenRefreshed = false
        val statuses = mutableListOf<String>()

        val (result, modelUsed) = GeminiAiService.executeWithLiteFallback(
            primaryTimeoutMs = 1_000L,
            fallbackTimeoutMs = 1_000L,
            onStatusUpdate = { statuses.add(it) },
            onRefreshToken = { tokenRefreshed = true },
            primaryCall = { "primary-ok" },
            fallbackCall = { "lite-ok" }
        )

        assertEquals("primary-ok", result)
        assertEquals(GeminiAiService.PRIMARY_MODEL, modelUsed)
        assertFalse(tokenRefreshed)
        assertTrue(statuses.isEmpty())
    }

    @Test
    fun executeWithLiteFallback_switchesToLiteOnTimeout() = runBlocking {
        val statuses = mutableListOf<String>()

        val (result, modelUsed) = GeminiAiService.executeWithLiteFallback(
            primaryTimeoutMs = 50L,
            fallbackTimeoutMs = 1_000L,
            onStatusUpdate = { statuses.add(it) },
            onRefreshToken = {},
            primaryCall = {
                delay(400L)
                "too-late"
            },
            fallbackCall = { "lite-fast-ok" }
        )

        assertEquals("lite-fast-ok", result)
        assertEquals(GeminiAiService.LITE_FALLBACK_MODEL, modelUsed)
        assertTrue(statuses.any { it.contains("Lite", ignoreCase = true) })
    }

    @Test
    fun executeWithLiteFallback_refreshesTokenAndRetriesLiteOnTokenError() = runBlocking {
        var tokenRefreshed = false
        val statuses = mutableListOf<String>()

        val (result, modelUsed) = GeminiAiService.executeWithLiteFallback(
            primaryTimeoutMs = 1_000L,
            fallbackTimeoutMs = 1_000L,
            onStatusUpdate = { statuses.add(it) },
            onRefreshToken = { tokenRefreshed = true },
            primaryCall = {
                throw IllegalStateException("Firebase App Check token error (403)")
            },
            fallbackCall = { "recovered-via-lite" }
        )

        assertTrue(tokenRefreshed)
        assertEquals("recovered-via-lite", result)
        assertEquals(GeminiAiService.LITE_FALLBACK_MODEL, modelUsed)
        assertTrue(statuses.any { it.contains("Refreshing", ignoreCase = true) })
    }

    @Test
    fun parseListingDraftJson_and_errorFormatting_workCleanly() {
        val raw = """
            {
              "objectTitle": "Sony Alpha A7 IV Mirrorless Camera",
              "categoryKey": "ELECTRONICS",
              "description": "Full-frame camera body with kit lens in clean condition.",
              "observedCondition": "Mint optical & body condition",
              "suggestedDailyRateInr": 1200,
              "recommendedDepositInr": 5000,
              "compatibleAccessories": ["64GB SD Card", "Extra NP-FZ100 Battery", "Peak Design Strap"]
            }
        """.trimIndent()

        val draft = GeminiAiService.parseListingDraftJson(raw)
        assertEquals("Sony Alpha A7 IV Mirrorless Camera", draft.objectTitle)
        assertEquals(RentalCategory.ELECTRONICS, draft.toRentalCategory())
        assertEquals(1200, draft.suggestedDailyRateInr)
        assertEquals(5000, draft.recommendedDepositInr)
        assertEquals(3, draft.compatibleAccessories.size)

        assertTrue(GeminiAiService.isTokenOrAppCheckError(RuntimeException("AppCheck attestation failed")))
        assertFalse(GeminiAiService.isTokenOrAppCheckError(RuntimeException("Socket closed")))
    }
}
