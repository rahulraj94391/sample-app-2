package com.example.instagram.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.instagram.database.entity.RecentSearch

@Dao
interface RecentSearchDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentSearch(search: RecentSearch): Long
    
    @Query("DELETE FROM recent_search")
    suspend fun deleteAllSearches()
    
    @Query("SELECT * FROM recent_search ORDER BY search_id DESC")
    fun getAllSearchedNames(): LiveData<List<RecentSearch>>
    
    @Query("DELETE FROM recent_search WHERE recent_search.profileId = :profileId")
    suspend fun deleteIfExist(profileId: Long)
    
    @Transaction
    suspend fun insertAndDeleteIfExist(search: RecentSearch) {
        deleteIfExist(search.profileId)
        insertRecentSearch(search)
    }
}