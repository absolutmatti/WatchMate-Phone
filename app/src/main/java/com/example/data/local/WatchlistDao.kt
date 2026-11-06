package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist_items ORDER BY addedAt DESC")
    fun getAllFlow(): Flow<List<WatchlistItem>>

    @Query("SELECT * FROM watchlist_items WHERE id = :id LIMIT 1")
    fun getItemByIdFlow(id: Long): Flow<WatchlistItem?>

    @Query("SELECT * FROM watchlist_items WHERE id = :id LIMIT 1")
    suspend fun getItemByIdDirect(id: Long): WatchlistItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WatchlistItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WatchlistItem>)

    @Update
    suspend fun update(item: WatchlistItem)

    @Delete
    suspend fun delete(item: WatchlistItem)

    @Query("DELETE FROM watchlist_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE watchlist_items SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE watchlist_items SET currentSeason = :season, currentEpisode = :episode, status = :status, lastWatchedAt = :lastWatchedAt WHERE id = :id")
    suspend fun updateEpisodeProgress(id: Long, season: Int, episode: Int, status: String, lastWatchedAt: Long)
}
