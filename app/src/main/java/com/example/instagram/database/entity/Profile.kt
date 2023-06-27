package com.example.instagram.database.entity

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "profile")
data class Profile(
    val first_name: String,
    val last_name: String,
    val dob: Date,
    val phone_number: String,
    val email_id: String,
    val bio: String,
    val gender: String,
    @Ignore
    val profile_img: Bitmap,
) {
    @PrimaryKey(autoGenerate = true)
    val profile_id: Int = 0
}