package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "post",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profileId"),
            childColumns = arrayOf("profileId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class Post(
    val profileId: Long,
    val postTime: Long,
    val placeId: String?,
) {
    @PrimaryKey(autoGenerate = true)
    var postId: Long = System.currentTimeMillis()
}