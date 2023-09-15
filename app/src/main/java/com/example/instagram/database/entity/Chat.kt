package com.example.instagram.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "chat")
data class Chat(
    var senderId: Long,
    var receiverId: Long,
    var message: String,
    var timeStamp: Long,
    var messageType: Int,
    var replyToChat: Long = -1,
) {
    @PrimaryKey(autoGenerate = true)
    var rowId: Long = 0
}

/*
Message type ->
1 -> Regular
2 -> Reply

3 -> Deleted
* */