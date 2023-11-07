package com.example.instagram.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "blocked_users",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profile_id"),
            childColumns = arrayOf("blockerId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profile_id"),
            childColumns = arrayOf("blockedId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BlockedUsers(
    val blockerId: Long,
    var blockedId: Long,
) {
    @PrimaryKey(autoGenerate = true)
    var rowId: Long = 0
}
