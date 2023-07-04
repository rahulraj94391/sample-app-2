package com.example.instagram.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "post",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profile_id"),
            childColumns = arrayOf("profile_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class Post(
    val profile_id: Long,
    val post_time: Long,
    @PrimaryKey(autoGenerate = true)
    val post_id: Long = 0,
)