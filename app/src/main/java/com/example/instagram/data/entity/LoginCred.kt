package com.example.instagram.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "login_credential",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profileId"),
            childColumns = arrayOf("profileId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LoginCred(
    var profileId: Long,
    var username: String,
    var password: String,
) {
    @PrimaryKey(autoGenerate = true)
    var loginCredId: Long = 0
}
