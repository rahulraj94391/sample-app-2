package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "likes",
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
            childColumns = arrayOf("profileId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Likes(
    val postId: Long,
    val profileId: Long,
    val likeTime: Long,
) {
    @PrimaryKey(autoGenerate = true)
    var likeId: Long = 0
}