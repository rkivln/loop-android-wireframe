package com.example.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class BookingDateUtilsTest {

    private val fixedTodayUtc = 1_794_960_000_000L // UTC midnight baseline

    @Test
    fun addDaysAndCalculateDuration_syncsAndClampsCorrectly() {
        val normalized = BookingDateUtils.normalizeToUtcMidnight(fixedTodayUtc)
        val end3Days = BookingDateUtils.addDaysUtcMillis(normalized, 3)
        assertEquals(3, BookingDateUtils.calculateDurationDays(normalized, end3Days))

        val endClampedMax = BookingDateUtils.addDaysUtcMillis(normalized, 99)
        assertEquals(BookingDateUtils.MAX_RENTAL_DAYS, BookingDateUtils.calculateDurationDays(normalized, endClampedMax))

        val endClampedMin = BookingDateUtils.addDaysUtcMillis(normalized, 0)
        assertEquals(BookingDateUtils.MIN_RENTAL_DAYS, BookingDateUtils.calculateDurationDays(normalized, endClampedMin))
    }

    @Test
    fun validateRange_rejectsPastSameDayAndTooLongRanges() {
        val today = BookingDateUtils.normalizeToUtcMidnight(fixedTodayUtc)
        val yesterday = today - BookingDateUtils.MILLIS_PER_DAY
        val tomorrow = today + BookingDateUtils.MILLIS_PER_DAY
        val valid3Days = BookingDateUtils.addDaysUtcMillis(today, 3)
        val overLimit = today + (35L * BookingDateUtils.MILLIS_PER_DAY)

        assertNull(BookingDateUtils.validateRange(today, tomorrow, today))
        assertNull(BookingDateUtils.validateRange(today, valid3Days, today))

        assertNotNull(BookingDateUtils.validateRange(null, tomorrow, today))
        assertNotNull(BookingDateUtils.validateRange(today, null, today))
        assertTrue(
            BookingDateUtils.validateRange(yesterday, tomorrow, today)!!
                .contains("past", ignoreCase = true)
        )
        assertTrue(
            BookingDateUtils.validateRange(today, today, today)!!
                .contains("after pickup", ignoreCase = true)
        )
        assertTrue(
            BookingDateUtils.validateRange(today, overLimit, today)!!
                .contains("30", ignoreCase = true)
        )
    }

    @Test
    fun formatUtcDate_formatsCleanReadableString() {
        val today = BookingDateUtils.normalizeToUtcMidnight(fixedTodayUtc)
        val formatted = BookingDateUtils.formatUtcDate(today, Locale.US)
        assertTrue("Expected readable MMM dd, yyyy format, got '$formatted'", formatted.matches(Regex("[A-Za-z]{3} \\d{2}, \\d{4}")))
    }
}
