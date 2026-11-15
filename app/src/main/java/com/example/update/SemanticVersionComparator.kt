package com.example.update

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object SemanticVersionComparator {
    private val repoPattern = Regex("^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$")

    fun isValidOwnerRepo(value: String): Boolean {
        return repoPattern.matches(value.trim())
    }

    fun normalizeOwnerRepoInput(rawInput: String): String? {
        var cleaned = rawInput.trim()
        if (cleaned.isEmpty()) return null

        cleaned = cleaned
            .removePrefix("https://github.com/")
            .removePrefix("http://github.com/")
            .removePrefix("github.com/")
            .trimStart('/')

        val segments = cleaned.split("/")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (segments.size < 2) return null

        val owner = segments[0]
        val repo = segments[1].removeSuffix(".git")
        val candidate = "$owner/$repo"
        return if (isValidOwnerRepo(candidate)) candidate else null
    }

    fun normalizeTag(rawTag: String): String {
        return rawTag.trim().removePrefix("v").removePrefix("V")
    }

    /**
     * Returns true if [remoteTag] represents a strictly newer version than [installedVersion].
     * Compares numeric version parts component-by-component (e.g. 1.2.0 > 1.0, 1.10 > 1.2)
     * and treats stable versions as newer than prereleases sharing the same core tuple.
     */
    fun isRemoteNewer(installedVersion: String, remoteTag: String): Boolean {
        val localParsed = parseVersion(installedVersion)
        val remoteParsed = parseVersion(remoteTag)

        if (localParsed.numbers.isEmpty() || remoteParsed.numbers.isEmpty()) {
            return normalizeTag(remoteTag) != normalizeTag(installedVersion) &&
                normalizeTag(remoteTag).isNotBlank()
        }

        val maxLen = maxOf(localParsed.numbers.size, remoteParsed.numbers.size)
        for (i in 0 until maxLen) {
            val l = localParsed.numbers.getOrElse(i) { 0 }
            val r = remoteParsed.numbers.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }

        // Core numeric versions match (e.g. 1.0 vs v1.0.0)
        if (localParsed.hasPrerelease && !remoteParsed.hasPrerelease) {
            return true
        }
        if (!localParsed.hasPrerelease && remoteParsed.hasPrerelease) {
            return false
        }
        return false
    }

    private data class ParsedVersion(
        val numbers: List<Int>,
        val hasPrerelease: Boolean
    )

    private fun parseVersion(raw: String): ParsedVersion {
        val strippedBuild = normalizeTag(raw).substringBefore("+")
        val corePart = strippedBuild.substringBefore("-")
        val hasPrerelease = strippedBuild.contains("-")
        val numbers = corePart.split(".")
            .mapNotNull { token ->
                val digits = token.takeWhile { it.isDigit() }
                digits.toIntOrNull()
            }
        return ParsedVersion(numbers = numbers, hasPrerelease = hasPrerelease)
    }

    fun formatIsoDateToReadable(isoTimestamp: String?): String {
        if (isoTimestamp.isNullOrBlank()) return "Recent release"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val parsed = parser.parse(isoTimestamp.trim()) ?: return isoTimestamp.take(10)
            val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            formatter.format(parsed)
        } catch (_: Exception) {
            isoTimestamp.take(10)
        }
    }

    fun formatByteSize(bytes: Long): String {
        if (bytes <= 0L) return ""
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return if (mb >= 1.0) {
            String.format(Locale.US, "%.1f MB", mb)
        } else {
            String.format(Locale.US, "%.0f KB", kb.coerceAtLeast(1.0))
        }
    }
}
