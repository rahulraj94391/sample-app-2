package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "last_online",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profile_id"),
            childColumns = arrayOf("sender"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profile_id"),
            childColumns = arrayOf("receiver"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LastOnline(
    var sender: Long,
    var receiver: Long,
    var time: Long,
) {
    @PrimaryKey(autoGenerate = true)
    var rowId: Long = 0
}
