package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityEventDao {
    @Query("SELECT * FROM community_events ORDER BY id ASC")
    fun getAllEventsFlow(): Flow<List<CommunityEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CommunityEventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CommunityEventEntity)

    @Query("DELETE FROM community_events")
    suspend fun clearEvents()
}
