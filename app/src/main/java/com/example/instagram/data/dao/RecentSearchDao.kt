package com.example.instagram.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.instagram.data.entity.RecentSearch

@Dao
interface RecentSearchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentSearch(search: RecentSearch): Long
    
    @Query("DELETE FROM recent_search")
    suspend fun deleteAllSearches()
    
    @Query("SELECT * FROM recent_search WHERE ownerId = :ownerId AND profileId NOT IN (Select blockerId from blocked_users where blockedId = :ownerId) ORDER BY search_id DESC")
    fun getAllSearchedNames(ownerId: Long): LiveData<List<RecentSearch>>
    
    @Query("DELETE FROM recent_search WHERE profileId = :profileId AND ownerId = :ownerId")
    suspend fun deleteIfExist(profileId: Long, ownerId: Long)
    
    @Transaction
    suspend fun insertAndDeleteIfExist(search: RecentSearch) {
        deleteIfExist(search.profileId, search.ownerId)
        insertRecentSearch(search)
    }
}