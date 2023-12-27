package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "hash_tags",
    foreignKeys = [
        ForeignKey(
            entity = Post::class,
            parentColumns = arrayOf("post_id"),
            childColumns = arrayOf("post_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HashTag(
    var post_id: Long,
    var tag: String,
) {
    @PrimaryKey(autoGenerate = true)
    var rowId = 0
}