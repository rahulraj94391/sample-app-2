package com.example.instagram.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.instagram.database.dao.CacheDao
import com.example.instagram.database.dao.CommentDao
import com.example.instagram.database.dao.FollowDao
import com.example.instagram.database.dao.LikesDao
import com.example.instagram.database.dao.LoginCredDao
import com.example.instagram.database.dao.PostDao
import com.example.instagram.database.dao.PostTextDao
import com.example.instagram.database.dao.ProfileDao
import com.example.instagram.database.dao.SavedPostDao
import com.example.instagram.database.dao.SearchDao
import com.example.instagram.database.dao.TagPeopleDao
import com.example.instagram.database.entity.Comment
import com.example.instagram.database.entity.Follow
import com.example.instagram.database.entity.ImageCache
import com.example.instagram.database.entity.Likes
import com.example.instagram.database.entity.LoginCred
import com.example.instagram.database.entity.Post
import com.example.instagram.database.entity.PostImage
import com.example.instagram.database.entity.PostText
import com.example.instagram.database.entity.Profile
import com.example.instagram.database.entity.SavedPost
import com.example.instagram.database.entity.Tag

@Database(entities = [Comment::class, Follow::class, Likes::class, LoginCred::class, Post::class, PostImage::class, PostText::class, Profile::class, SavedPost::class, Tag::class, ImageCache::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commentDao(): CommentDao
    abstract fun followDao(): FollowDao
    abstract fun likesDao(): LikesDao
    abstract fun loginCredDao(): LoginCredDao
    abstract fun postDao(): PostDao
    abstract fun profileDao(): ProfileDao
    abstract fun savedPostDao(): SavedPostDao
    abstract fun tagPeopleDao(): TagPeopleDao
    abstract fun searchDao(): SearchDao
    abstract fun postTextDao(): PostTextDao
    abstract fun cacheDao(): CacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(applicationContext: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room
                        .databaseBuilder(applicationContext, AppDatabase::class.java, "instaDB")
                        .build()
                }
            }
            return INSTANCE!!
        }
    }
}