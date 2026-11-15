package com.example.update

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import java.io.IOException
import java.util.concurrent.TimeUnit

interface GitHubReleasesApi {
    @Headers(
        "Accept: application/vnd.github+json",
        "X-GitHub-Api-Version: 2022-11-28"
    )
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("User-Agent") userAgent: String = "Loop-Android/${BuildConfig.VERSION_NAME}"
    ): Response<GitHubReleaseDto>
}

object GitHubReleasesService {
    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .build()
    }

    private val api: GitHubReleasesApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GitHubReleasesApi::class.java)
    }

    suspend fun fetchLatestRelease(ownerRepo: String): Result<GitHubReleaseInfo> {
        val normalizedRepo = SemanticVersionComparator.normalizeOwnerRepoInput(ownerRepo)
            ?: return Result.failure(
                IllegalArgumentException("Invalid GitHub repository format. Use owner/repo.")
            )
        val parts = normalizedRepo.split("/")
        val owner = parts[0]
        val repo = parts[1]

        return withContext(Dispatchers.IO) {
            try {
                val response = api.getLatestRelease(owner = owner, repo = repo)
                if (!response.isSuccessful) {
                    val errorMsg = when (response.code()) {
                        404 -> "No published release found for $normalizedRepo (or repo is private)."
                        403, 429 -> "GitHub API rate limit reached. Please try again later."
                        else -> "GitHub returned HTTP ${response.code()} while checking updates."
                    }
                    return@withContext Result.failure(IllegalStateException(errorMsg))
                }

                val dto = response.body()
                    ?: return@withContext Result.failure(
                        IllegalStateException("Empty release response received from GitHub.")
                    )

                if (dto.draft || dto.prerelease) {
                    return@withContext Result.failure(
                        IllegalStateException("Latest release entry on $normalizedRepo is a draft or prerelease.")
                    )
                }

                if (dto.tagName.isBlank()) {
                    return@withContext Result.failure(
                        IllegalStateException("Latest GitHub release is missing a version tag.")
                    )
                }

                val releaseUrl = dto.htmlUrl.trim().takeIf {
                    it.startsWith("https://", ignoreCase = true)
                } ?: "https://github.com/$normalizedRepo/releases/latest"

                val apkAsset = dto.assets.firstOrNull {
                    it.name.endsWith(".apk", ignoreCase = true)
                }

                val info = GitHubReleaseInfo(
                    tagName = dto.tagName.trim(),
                    normalizedVersion = SemanticVersionComparator.normalizeTag(dto.tagName),
                    releaseTitle = dto.name?.trim()?.takeIf { it.isNotBlank() } ?: dto.tagName.trim(),
                    notes = dto.body?.trim()?.takeIf { it.isNotBlank() }
                        ?: "Bug fixes, reliability improvements, and community updates.",
                    htmlUrl = releaseUrl,
                    publishedAtLabel = SemanticVersionComparator.formatIsoDateToReadable(dto.publishedAt),
                    apkAssetName = apkAsset?.name?.takeIf { it.isNotBlank() },
                    apkAssetSizeBytes = apkAsset?.size?.takeIf { it > 0L }
                )
                Result.success(info)
            } catch (_: IOException) {
                Result.failure(
                    IOException("Unable to reach GitHub. Check your internet connection.")
                )
            } catch (e: Exception) {
                Result.failure(
                    IllegalStateException(e.localizedMessage ?: "Failed to inspect GitHub releases.")
                )
            }
        }
    }
}
