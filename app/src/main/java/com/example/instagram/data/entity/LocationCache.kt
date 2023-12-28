package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locationCache")
data class LocationCache(
    @PrimaryKey
    val placeId: String,
    val fullText: String,
    val primaryText: String?,
    val secondaryText: String?,
) {
    fun toLocation(): Location {
        return Location(
            placeId,
            primaryText,
            secondaryText
        )
    }
}
