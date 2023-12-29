package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "post_image",
    foreignKeys = [
        ForeignKey(
            entity = Post::class,
            parentColumns = arrayOf("postId"),
            childColumns = arrayOf("postId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PostImage(
    val postId: Long,
    val postImagePath: String,
    @PrimaryKey(autoGenerate = true)
    val rowId: Long = 0,
)
