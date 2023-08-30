package com.example.instagram.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_search")
data class RecentSearch(
    val profileId: Long,
    val firstName: String,
) {
    @PrimaryKey(autoGenerate = true)
    var search_id: Long = 0
}
