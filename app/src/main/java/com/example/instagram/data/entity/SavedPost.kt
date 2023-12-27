package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "saved_post",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profile_id"),
            childColumns = arrayOf("profile_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Post::class,
            parentColumns = arrayOf("post_id"),
            childColumns = arrayOf("post_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class SavedPost(
    val profile_id: Long,
    val post_id: Long,
    val time: Long
) {
    @PrimaryKey(autoGenerate = true)
    var saved_post_id: Long = 0
}
