package com.example.instagram.data.entity

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class Profile(
    var firstName: String,
    var lastName: String,
    var dob: Long,
    var phoneNumber: String,
    var emailId: String,
    var bio: String?,
    var gender: String,
    @Ignore var profileImg: Bitmap? = null,
    @PrimaryKey var profileId: Long,
) {
    constructor(firstName: String, lastName: String, dob: Long, phoneNumber: String, emailId: String, gender: String)
            : this(firstName, lastName, dob, phoneNumber, emailId, null, gender, null, 0)
}