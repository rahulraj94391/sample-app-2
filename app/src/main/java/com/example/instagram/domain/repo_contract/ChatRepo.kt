package com.example.instagram.domain.repo_contract

import androidx.lifecycle.LiveData
import com.example.instagram.database.entity.Chat

interface ChatRepo {
    suspend fun getChats(userId: Long, myId: Long, offset: Int): MutableList<Chat>
    suspend fun getChat(chatId: Long): Chat
    suspend fun insertNewChat(chat: Chat): Long
    suspend fun markChatsAsDeleted(chats: List<Chat>): Int
    fun getMyLatestChatsPerUser(myId: Long): LiveData<List<Chat>>
}