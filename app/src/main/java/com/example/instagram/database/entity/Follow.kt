package com.example.instagram.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey


@Entity(
    tableName = "follow",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profile_id"),
            childColumns = arrayOf("owner_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("profile_id"),
            childColumns = arrayOf("follower_id"),
            onDelete = ForeignKey.CASCADE
        )]
)
data class Follow(
    val owner_id: Int,
    val follower_id: Int,
)
