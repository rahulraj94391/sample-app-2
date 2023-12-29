package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "chat",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profileId"),
            childColumns = arrayOf("senderId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profileId"),
            childColumns = arrayOf("receiverId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Chat(
    var senderId: Long,
    var receiverId: Long,
    var message: String,
    var timeStamp: Long,
    var messageType: Int, // 1 -> Regular, 2 -> Reply, 3 -> Deleted
    var replyToChat: Long = -1,
) {
    @PrimaryKey(autoGenerate = true)
    var rowId: Long = 0
    
    /*override fun equals(o: Any?): Boolean {
        if (o !is Chat) {
            return false
        }
        val thisRef = "${senderId.coerceAtMost(receiverId)}_${senderId.coerceAtLeast(receiverId)}"
        val otherRef = "${o.senderId.coerceAtMost(o.receiverId)}_${o.senderId.coerceAtLeast(o.receiverId)}"
        return thisRef == otherRef
    }*/
    
    /*override fun hashCode(): Int {
        val result = (17 * senderId * receiverId) % 1e9
        return result.toInt()
    }*/
}
