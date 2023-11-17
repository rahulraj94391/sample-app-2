package com.example.instagram.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.instagram.database.entity.Chat

@Dao()
interface ChatsDao {
    @Query("select * from chat where chat.senderId = :userId AND chat.receiverId = :myId OR chat.senderId = :myId AND chat.receiverId = :userId order by chat.timeStamp desc Limit 30 Offset :offset")
    suspend fun getChats(userId: Long, myId: Long, offset: Int): MutableList<Chat>
    
    @Query("select * from chat where chat.rowId = :chatId")
    suspend fun getChat(chatId: Long): Chat
    
    @Insert
    suspend fun insertNewChat(chat: Chat): Long
    
    @Update
    suspend fun markChatsAsDeleted(chats: List<Chat>): Int
    
    @Query("SELECT * from chat where senderId = :myId or receiverId = :myId ORDER by timeStamp DESC")
    fun getMyLatestChatsPerUser(myId: Long): LiveData<List<Chat>>
    
    
    
}

