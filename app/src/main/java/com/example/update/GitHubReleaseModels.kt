package com.example.update

import com.squareup.moshi.Json

data class GitHubReleaseAssetDto(
    @Json(name = "name") val name: String = "",
    @Json(name = "size") val size: Long = 0L,
    @Json(name = "browser_download_url") val browserDownloadUrl: String = ""
)

data class GitHubReleaseDto(
    @Json(name = "tag_name") val tagName: String = "",
    @Json(name = "name") val name: String? = null,
    @Json(name = "body") val body: String? = null,
    @Json(name = "html_url") val htmlUrl: String = "",
    @Json(name = "published_at") val publishedAt: String? = null,
    @Json(name = "draft") val draft: Boolean = false,
    @Json(name = "prerelease") val prerelease: Boolean = false,
    @Json(name = "assets") val assets: List<GitHubReleaseAssetDto> = emptyList()
)

data class GitHubReleaseInfo(
    val tagName: String,
    val normalizedVersion: String,
    val releaseTitle: String,
    val notes: String,
    val htmlUrl: String,
    val publishedAtLabel: String,
    val apkAssetName: String? = null,
    val apkAssetSizeBytes: Long? = null
)

sealed class AppUpdateUiState {
    abstract val installedVersion: String

    data class NotConfigured(
        override val installedVersion: String
    ) : AppUpdateUiState()

    data class Checking(
        override val installedVersion: String,
        val configuredRepo: String,
        val isManual: Boolean
    ) : AppUpdateUiState()

    data class UpToDate(
        override val installedVersion: String,
        val configuredRepo: String,
        val latestTag: String?,
        val lastCheckedLabel: String
    ) : AppUpdateUiState()

    data class UpdateAvailable(
        override val installedVersion: String,
        val configuredRepo: String,
        val release: GitHubReleaseInfo,
        val isBannerDismissedForSession: Boolean = false,
        val isVersionSkipped: Boolean = false
    ) : AppUpdateUiState()

    data class Error(
        override val installedVersion: String,
        val configuredRepo: String,
        val message: String,
        val isManual: Boolean
    ) : AppUpdateUiState()
}
