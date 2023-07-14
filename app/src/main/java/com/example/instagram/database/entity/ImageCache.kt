package com.example.instagram.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ImageCache(
    val imageUrl: String,
    val time: Long
) {
    @PrimaryKey(autoGenerate = true)
    var fileName: Long = 0
}
