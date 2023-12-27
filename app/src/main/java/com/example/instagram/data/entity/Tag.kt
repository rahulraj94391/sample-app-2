package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tag",
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
            childColumns = arrayOf("profile_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class Tag(
    val post_id: Long,
    val profile_id: Long,

    ) {
    @PrimaryKey(autoGenerate = true)
    var tag_id: Long = 0
}
