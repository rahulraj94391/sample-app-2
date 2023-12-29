package com.example.instagram.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.data.entity.Tag

@Dao
interface TagPeopleDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPostTags(tag: MutableList<Tag>)
    
    @Query("SELECT postId FROM tag WHERE profileId = :profileId Order by rowId Desc LIMIT :limit OFFSET :offset")
    suspend fun getAllTaggedPostOfProfile(profileId: Long, limit: Int = 15, offset: Int): MutableList<Long>
    
}