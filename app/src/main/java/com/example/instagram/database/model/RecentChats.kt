package com.example.instagram.database.model

data class RecentChats(
    var senderId: Long,
    var receiverId: Long,
    var message: String,
    var timeStamp: Long,
    var messageType: Int, // 1 -> Regular, 2 -> Reply, 3 -> Deleted
    var replyToChat: Long = -1,
    var rowId: Long = 0,
) {
    override fun equals(other: Any?): Boolean {
        if (other !is RecentChats) {
            return false
        }
        val thisRef = "${senderId.coerceAtMost(receiverId)}_${senderId.coerceAtLeast(receiverId)}"
        val otherRef = "${other.senderId.coerceAtMost(other.receiverId)}_${other.senderId.coerceAtLeast(other.receiverId)}"
        return thisRef == otherRef
    }
    
    override fun hashCode(): Int {
        val result = (17 * senderId * receiverId) % 1e9
        return result.toInt()
    }
}
