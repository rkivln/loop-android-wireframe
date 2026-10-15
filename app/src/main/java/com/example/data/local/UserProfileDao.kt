package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local persistence of User Profiles.
 */
@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profiles WHERE uid = :uid LIMIT 1")
    fun getProfileFlow(uid: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE uid = :uid LIMIT 1")
    suspend fun getProfile(uid: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Query("DELETE FROM user_profiles WHERE uid = :uid")
    suspend fun deleteProfile(uid: String)
}
