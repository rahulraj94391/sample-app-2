package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "login_credential",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profile_id"),
            childColumns = arrayOf("profile_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LoginCred(
    var profile_id: Long,
    var username: String,
    var password: String,
) {
    @PrimaryKey(autoGenerate = true)
    var login_cred_id: Long = 0
}
