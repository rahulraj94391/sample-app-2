package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.database.entity.Tag

@Dao
interface TagPeopleDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPostTags(tag: MutableList<Tag>)

    @Query("SELECT post_id FROM tag WHERE profile_id = :profileId LIMIT 15 OFFSET :offset")
    suspend fun getAllTaggedPostOfProfile(profileId: Long, offset: Int): MutableList<Long>

}