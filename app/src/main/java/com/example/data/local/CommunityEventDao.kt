package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityEventDao {
    @Query("SELECT * FROM community_events ORDER BY id DESC")
    fun getAllEvents(): Flow<List<CommunityEventEntity>>

    @Query("SELECT * FROM community_events WHERE status = 'ACTIVE' ORDER BY id DESC")
    fun getActiveEvents(): Flow<List<CommunityEventEntity>>

    @Query("SELECT COUNT(*) FROM community_events")
    suspend fun getCount(): Int

    @Query("SELECT * FROM community_events WHERE id = :id")
    fun getEventById(id: String): Flow<CommunityEventEntity?>

    @Query("SELECT * FROM community_events WHERE id = :id LIMIT 1")
    suspend fun getEventByIdOnce(id: String): CommunityEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CommunityEventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CommunityEventEntity)

    @Query("UPDATE community_events SET isSynced = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean)

    @Query("UPDATE community_events SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE community_events SET attendeesOrResponses = :attendees WHERE id = :id")
    suspend fun updateAttendees(id: String, attendees: String)

    @Query("UPDATE community_events SET attendeesOrResponses = :attendees, attendeeIdsCsv = :attendeeIdsCsv WHERE id = :id")
    suspend fun updateAttendeesAndIds(id: String, attendees: String, attendeeIdsCsv: String)

    @Query("DELETE FROM community_events WHERE id = :id")
    suspend fun deleteEventById(id: String)

    @Query("DELETE FROM community_events")
    suspend fun clearEvents()
}
