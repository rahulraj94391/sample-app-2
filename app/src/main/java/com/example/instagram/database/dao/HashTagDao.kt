package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface HashTagDao {
    
    @Insert
    suspend fun inset(postId: Long, tags: List<String>): List<Long>
    
    
    
}