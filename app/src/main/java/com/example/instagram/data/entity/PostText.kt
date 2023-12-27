package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "post_text",
    foreignKeys = [
        ForeignKey(
            entity = Post::class,
            parentColumns = arrayOf("post_id"),
            childColumns = arrayOf("post_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class PostText(
    val post_id: Long,
    val post_text: String,

    ) {
    @PrimaryKey(autoGenerate = true)
    var post_text_id: Long = 0
}