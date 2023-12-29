package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "follow",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profileId"),
            childColumns = arrayOf("ownerId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profileId"),
            childColumns = arrayOf("followerId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Follow(
    val ownerId: Long,
    val followerId: Long,
    val time:Long
) {
    @PrimaryKey(autoGenerate = true)
    var followRowId: Long = 0
}
