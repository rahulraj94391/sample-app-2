package com.example.instagram.database.entity

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class Profile(
    var first_name: String,
    var last_name: String,
    var dob: Long,
    var phone_number: String,
    var email_id: String,
    var bio: String,
    var gender: String,
    @Ignore
    var profile_img: Bitmap? = null,
    @PrimaryKey(autoGenerate = true)
    var profile_id: Int,
) {
    constructor(first_name: String, last_name: String, dob: Long, phone_number: String, email_id: String, bio: String, gender: String)
            : this(first_name, last_name, dob, phone_number, email_id, bio, gender, null, 0)
}