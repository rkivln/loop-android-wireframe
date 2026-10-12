package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local persistence of User Profile in Room.
 */
@Dao
interface UserProfileDao {

    /**
     * Observes the user profile reactively as a [Flow].
     */
    @Query("SELECT * FROM user_profiles WHERE uid = :uid LIMIT 1")
    fun getUserProfile(uid: String = "user_me"): Flow<UserProfileEntity?>

    /**
     * Reads the current user profile once.
     */
    @Query("SELECT * FROM user_profiles WHERE uid = :uid LIMIT 1")
    suspend fun getUserProfileOnce(uid: String = "user_me"): UserProfileEntity?

    /**
     * Inserts or updates the user profile.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    /**
     * Updates editable personal profile information: display name, bio, avatar preset, and location.
     */
    @Query("""
        UPDATE user_profiles 
        SET displayName = :displayName,
            bio = :bio,
            avatarPresetIndex = :avatarPresetIndex,
            location = :location,
            phone = :phone,
            lastUpdatedEpoch = :lastUpdatedEpoch
        WHERE uid = :uid
    """)
    suspend fun updateBasicProfile(
        uid: String = "user_me",
        displayName: String,
        bio: String,
        avatarPresetIndex: Int,
        location: String,
        phone: String,
        lastUpdatedEpoch: Long = System.currentTimeMillis()
    )

    /**
     * Deletes the user profile record.
     */
    @Query("DELETE FROM user_profiles WHERE uid = :uid")
    suspend fun deleteProfile(uid: String = "user_me")
}
