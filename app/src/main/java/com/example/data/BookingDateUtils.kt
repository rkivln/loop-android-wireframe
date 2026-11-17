package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

object BookingDateUtils {
    const val MILLIS_PER_DAY = 86_400_000L
    const val MIN_RENTAL_DAYS = 1
    const val MAX_RENTAL_DAYS = 30

    private val utcTimeZone: TimeZone = TimeZone.getTimeZone("UTC")

    fun todayUtcMidnightMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        val localCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val utcCal = Calendar.getInstance(utcTimeZone).apply {
            clear()
            set(
                localCal.get(Calendar.YEAR),
                localCal.get(Calendar.MONTH),
                localCal.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0
            )
            set(Calendar.MILLISECOND, 0)
        }
        return utcCal.timeInMillis
    }

    fun normalizeToUtcMidnight(utcMillis: Long): Long {
        val utcCal = Calendar.getInstance(utcTimeZone).apply {
            timeInMillis = utcMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return utcCal.timeInMillis
    }

    fun addDaysUtcMillis(startUtcMillis: Long, days: Int): Long {
        val clamped = days.coerceIn(MIN_RENTAL_DAYS, MAX_RENTAL_DAYS)
        return normalizeToUtcMidnight(startUtcMillis) + clamped * MILLIS_PER_DAY
    }

    fun calculateDurationDays(startUtcMillis: Long, endUtcMillis: Long): Int {
        val start = normalizeToUtcMidnight(startUtcMillis)
        val end = normalizeToUtcMidnight(endUtcMillis)
        val diffDays = ((end - start).toDouble() / MILLIS_PER_DAY.toDouble()).roundToInt()
        return diffDays.coerceIn(MIN_RENTAL_DAYS, MAX_RENTAL_DAYS)
    }

    fun validateRange(
        startUtcMillis: Long?,
        endUtcMillis: Long?,
        todayUtcMillis: Long = todayUtcMidnightMillis()
    ): String? {
        if (startUtcMillis == null || endUtcMillis == null) {
            return "Select both pickup and return dates."
        }
        val start = normalizeToUtcMidnight(startUtcMillis)
        val end = normalizeToUtcMidnight(endUtcMillis)
        if (start < todayUtcMillis) {
            return "Pickup date cannot be in the past."
        }
        if (end <= start) {
            return "Return date must be at least 1 day after pickup."
        }
        val diffDays = ((end - start) / MILLIS_PER_DAY).toInt()
        if (diffDays > MAX_RENTAL_DAYS) {
            return "Maximum rental duration is $MAX_RENTAL_DAYS days."
        }
        return null
    }

    fun formatUtcDate(utcMillis: Long, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy", locale).apply {
            timeZone = utcTimeZone
        }
        return formatter.format(java.util.Date(normalizeToUtcMidnight(utcMillis)))
    }
}
