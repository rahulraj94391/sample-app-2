package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.instagram.database.entity.Tag

@Dao
interface TagPeopleDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPostTags(tag: MutableList<Tag>)
}