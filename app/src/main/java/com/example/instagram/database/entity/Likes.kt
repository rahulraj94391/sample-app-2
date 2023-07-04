package com.example.instagram.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "likes",
    foreignKeys = [
        ForeignKey(
            entity = Post::class,
            parentColumns = arrayOf("post_id"),
            childColumns = arrayOf("post_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profile_id"),
            childColumns = arrayOf("profile_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Likes(
    val post_id: Long,
    val profile_id: Long,
    val like_time: Long,
    @PrimaryKey(autoGenerate = true)
    val like_id: Long = 0
)