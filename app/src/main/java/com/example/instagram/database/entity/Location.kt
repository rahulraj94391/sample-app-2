package com.example.instagram.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location")
data class Location(
    @PrimaryKey
    val placeId: String,
    val primaryText: String?,
    val secondaryText: String?,
)