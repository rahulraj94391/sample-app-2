package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.instagram.database.entity.HashTag

@Dao
interface HashTagDao {
    
    @Insert()
    suspend fun inset(hashTags: MutableList<HashTag>): List<Long>
    
    @Query("SELECT DISTINCT tag FROM hash_tags WHERE tag LIKE '%' || :tag || '%'")
    suspend fun getHashTags(tag: String): MutableList<String>
    
    @Query("SELECT post_id FROM hash_tags WHERE tag = :hashTag AND post_id IN (SELECT post_id FROM post WHERE profile_id != :myId AND profile_id NOT IN (SELECT blockerId FROM blocked_users WHERE blockedId = :myId)) ORDER BY rowId DESC LIMIT :limit OFFSET :offset")
    suspend fun getPostIds(hashTag: String, myId: Long, limit: Int, offset: Int): MutableList<Long>
    
}