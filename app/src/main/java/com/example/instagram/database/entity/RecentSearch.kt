package com.example.instagram.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "recent_search",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profile_id"),
            childColumns = arrayOf("profileId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profile_id"),
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
    var search_id: Long = 0
}
