package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "postText",
    foreignKeys = [
        ForeignKey(
            entity = Post::class,
            parentColumns = arrayOf("postId"),
            childColumns = arrayOf("postId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class PostText(
    val postId: Long,
    val postText: String,
    
    ) {
    @PrimaryKey(autoGenerate = true)
    var rowId: Long = 0
}