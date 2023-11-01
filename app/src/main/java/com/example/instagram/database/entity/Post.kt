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
        ),
        ForeignKey(
            entity = Location::class,
            parentColumns = arrayOf("placeId"),
            childColumns = arrayOf("locationId"),
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)

data class Post(
    val profile_id: Long,
    val post_time: Long,
    val locationId: String,
) {
    @PrimaryKey(autoGenerate = true)
    var post_id: Long = System.currentTimeMillis()
}