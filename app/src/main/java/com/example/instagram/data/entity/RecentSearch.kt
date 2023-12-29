package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "recent_search",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profileId"),
            childColumns = arrayOf("profileId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profileId"),
            childColumns = arrayOf("ownerId"),
            onDelete = ForeignKey.CASCADE
        )
    ]

)
data class RecentSearch(
    val profileId: Long,
    val firstName: String,
    val ownerId: Long,
) {
    @PrimaryKey(autoGenerate = true)
    var rowId: Long = 0
}
