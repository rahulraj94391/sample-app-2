package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tag",
    foreignKeys = [
        ForeignKey(
            entity = Post::class,
            parentColumns = arrayOf("postId"),
            childColumns = arrayOf("postId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profileId"),
            childColumns = arrayOf("profileId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class Tag(
    val postId: Long,
    val profileId: Long,
) {
    @PrimaryKey(autoGenerate = true)
    var rowId: Long = 0
}
