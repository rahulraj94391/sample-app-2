package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.database.entity.Post

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPost(post: Post): Long

    @Query("SELECT post_id FROM post WHERE profile_id = :profileId")
    suspend fun getAllPostOfProfile(profileId: Long): MutableList<Long>



}