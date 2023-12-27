package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "post_image",
    foreignKeys = [
        ForeignKey(
            entity = Post::class,
            parentColumns = arrayOf("post_id"),
            childColumns = arrayOf("post_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PostImage(
    val post_id: Long,
    val post_image_path: String,
    @PrimaryKey(autoGenerate = true)
    val post_img_id: Long = 0,
)
