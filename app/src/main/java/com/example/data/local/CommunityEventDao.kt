package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local community events database table.
 */
@Dao
interface CommunityEventDao {

    @Query("SELECT * FROM community_events ORDER BY createdAtEpoch DESC")
    fun getAllEvents(): Flow<List<CommunityEventEntity>>

    @Query("SELECT * FROM community_events WHERE status = 'ACTIVE' ORDER BY createdAtEpoch DESC")
    fun getActiveEvents(): Flow<List<CommunityEventEntity>>

    @Query("SELECT * FROM community_events WHERE type = :type ORDER BY createdAtEpoch DESC")
    fun getEventsByType(type: String): Flow<List<CommunityEventEntity>>

    @Query("SELECT * FROM community_events WHERE id = :id LIMIT 1")
    fun getEventById(id: String): Flow<CommunityEventEntity?>

    @Query("SELECT * FROM community_events WHERE id = :id LIMIT 1")
    suspend fun getEventByIdOnce(id: String): CommunityEventEntity?

    @Query("SELECT COUNT(*) FROM community_events")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CommunityEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CommunityEventEntity>)

    @Update
    suspend fun updateEvent(event: CommunityEventEntity)

    @Query("UPDATE community_events SET status = :status, lastUpdatedEpoch = :updatedEpoch WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedEpoch: Long = System.currentTimeMillis())

    @Query("UPDATE community_events SET attendeesOrResponses = :attendeesText, lastUpdatedEpoch = :updatedEpoch WHERE id = :id")
    suspend fun updateAttendees(id: String, attendeesText: String, updatedEpoch: Long = System.currentTimeMillis())

    @Query("UPDATE community_events SET isSyncedToFirestore = :isSynced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean)

    @Query("DELETE FROM community_events WHERE id = :id")
    suspend fun deleteEventById(id: String)

    @Query("DELETE FROM community_events")
    suspend fun deleteAllEvents()
}
