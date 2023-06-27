package com.example.instagram.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date


@Entity(
    tableName = "comment",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
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
    val comment_time: Date,
) {
    @PrimaryKey(autoGenerate = true)
    val comment_id: Int = 0
}
