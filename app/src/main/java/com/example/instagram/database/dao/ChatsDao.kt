package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.instagram.database.entity.Chat

@Dao()
interface ChatsDao {
    @Query("select * from chat where chat.senderId = :userId AND chat.receiverId = :myId OR chat.senderId = :myId AND chat.receiverId = :userId order by chat.timeStamp desc Limit 30 Offset :offset")
    suspend fun getChats(userId: Long, myId: Long, offset: Int): MutableList<Chat>
    
    @Query("select * from chat where chat.rowId = :chatId")
    suspend fun getChat(chatId: Long): Chat
    
    @Insert
    suspend fun insertNewChat(chat: Chat)
}