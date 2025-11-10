package com.po4yka.heauton.data.local.database.dao

import androidx.room.*
import com.po4yka.heauton.data.local.database.entities.UserEventEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for user event tracking.
 */
@Dao
interface UserEventDao {

    /**
     * Inserts a new user event.
     * @param event The event entity to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: UserEventEntity)

    /**
     * Retrieves all events of a specific type.
     * @param eventType The type of event to retrieve
     * @return Flow of events of the specified type
     */
    @Query("SELECT * FROM user_events WHERE eventType = :eventType ORDER BY timestamp DESC")
    fun getEventsByType(eventType: String): Flow<List<UserEventEntity>>

    /**
     * Retrieves events for a specific entity.
     * @param entityId The related entity ID
     * @return Flow of events for the entity
     */
    @Query("SELECT * FROM user_events WHERE relatedEntityId = :entityId ORDER BY timestamp DESC")
    fun getEventsForEntity(entityId: String): Flow<List<UserEventEntity>>

    /**
     * Gets events within a date range.
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @return Flow of events in the time range
     */
    @Query("SELECT * FROM user_events WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getEventsInRange(startTime: Long, endTime: Long): Flow<List<UserEventEntity>>

    /**
     * Counts events of a specific type.
     * @param eventType The event type to count
     * @return The count of events
     */
    @Query("SELECT COUNT(*) FROM user_events WHERE eventType = :eventType")
    suspend fun countEventsByType(eventType: String): Int

    /**
     * Deletes events older than a specified timestamp.
     * @param timestamp Events older than this will be deleted
     */
    @Query("DELETE FROM user_events WHERE timestamp < :timestamp")
    suspend fun deleteOldEvents(timestamp: Long)

    /**
     * Deletes all events.
     */
    @Query("DELETE FROM user_events")
    suspend fun deleteAll()
}
