package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "comment",
    foreignKeys = [
        ForeignKey(
            entity = Post::class,
            parentColumns = arrayOf("postId"),
            childColumns = arrayOf("postId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profileId"),
            childColumns = arrayOf("commenterId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Comment(
    val postId: Long,
    val commenterId: Long,
    val commentText: String,
    val commentTime: Long,
    
    ) {
    @PrimaryKey(autoGenerate = true)
    var commentId: Long = 0
}
