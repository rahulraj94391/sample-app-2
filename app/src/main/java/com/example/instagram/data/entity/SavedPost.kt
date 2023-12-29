package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "saved_post",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profileId"),
            childColumns = arrayOf("profileId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Post::class,
            parentColumns = arrayOf("postId"),
            childColumns = arrayOf("postId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class SavedPost(
    val profileId: Long,
    val postId: Long,
    val time: Long
) {
    @PrimaryKey(autoGenerate = true)
    var rowId: Long = 0
}
