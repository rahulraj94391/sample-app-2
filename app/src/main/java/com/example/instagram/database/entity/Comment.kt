package com.example.instagram.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "comment",
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
            childColumns = arrayOf("commenter_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Comment(
    val post_id: Int,
    val commenter_id: Int,
    val comment_text: String,
    val comment_time: Long,
    @PrimaryKey(autoGenerate = true)
    val comment_id: Int = 0,
)
