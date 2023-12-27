package com.example.instagram.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.instagram.data.entity.BlockedUsers

@Dao
interface BlockDao {
    @Query("select count(rowId) from blocked_users where blockerId = :userA and blockedId = :userB")
    suspend fun isBlocked(userA: Long, userB: Long): Int
    
    @Query("delete from blocked_users where blockerId = :myId and blockedId = :userId")
    suspend fun unblockUser(myId: Long, userId: Long): Int
    
    @Insert
    suspend fun blockUser(blockedUsers: BlockedUsers): Long
}