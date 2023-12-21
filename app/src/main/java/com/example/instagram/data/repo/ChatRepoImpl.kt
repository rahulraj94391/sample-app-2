package com.example.instagram.data.repo

import androidx.lifecycle.LiveData
import com.example.instagram.database.dao.ChatsDao
import com.example.instagram.database.entity.Chat
import com.example.instagram.domain.repo_contract.ChatRepo

class ChatRepoImpl(private val dao: ChatsDao) : ChatRepo {
    override suspend fun getChats(userId: Long, myId: Long, offset: Int): MutableList<Chat> {
        return dao.getChats(userId, myId, offset)
    }
    
    override suspend fun getChat(chatId: Long): Chat {
        return dao.getChat(chatId)
    }
    
    override suspend fun insertNewChat(chat: Chat): Long {
        return dao.insertNewChat(chat)
    }
    
    override suspend fun markChatsAsDeleted(chats: List<Chat>): Int {
        return dao.markChatsAsDeleted(chats)
    }
    
    override fun getMyLatestChatsPerUser(myId: Long): LiveData<List<Chat>> {
        return dao.getMyLatestChatsPerUser(myId)
    }
}