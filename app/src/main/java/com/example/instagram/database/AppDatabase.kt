package com.example.instagram.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.instagram.database.dao.CacheDao
import com.example.instagram.database.dao.ChatsDao
import com.example.instagram.database.dao.CommentDao
import com.example.instagram.database.dao.FollowDao
import com.example.instagram.database.dao.LastOnlineDao
import com.example.instagram.database.dao.LikesDao
import com.example.instagram.database.dao.LoginCredDao
import com.example.instagram.database.dao.PostDao
import com.example.instagram.database.dao.PostTextDao
import com.example.instagram.database.dao.ProfileDao
import com.example.instagram.database.dao.RecentSearchDAO
import com.example.instagram.database.dao.SavedPostDao
import com.example.instagram.database.dao.SearchDao
import com.example.instagram.database.dao.TagPeopleDao
import com.example.instagram.database.entity.Chat
import com.example.instagram.database.entity.Comment
import com.example.instagram.database.entity.Follow
import com.example.instagram.database.entity.ImageCache
import com.example.instagram.database.entity.LastOnline
import com.example.instagram.database.entity.Likes
import com.example.instagram.database.entity.LoginCred
import com.example.instagram.database.entity.Post
import com.example.instagram.database.entity.PostImage
import com.example.instagram.database.entity.PostText
import com.example.instagram.database.entity.Profile
import com.example.instagram.database.entity.RecentSearch
import com.example.instagram.database.entity.SavedPost
import com.example.instagram.database.entity.Tag

private const val TAG = "AppDatabase_CommTag"

@Database(
    entities = [
        Comment::class,
        Follow::class,
        Likes::class,
        LoginCred::class,
        Post::class,
        PostImage::class,
        PostText::class,
        Profile::class,
        SavedPost::class,
        Tag::class,
        ImageCache::class,
        RecentSearch::class,
        Chat::class,
        LastOnline::class
    ],
    version = 1,
    exportSchema = true
)
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
    abstract fun chatDao(): ChatsDao
    abstract fun recentSearchDao(): RecentSearchDAO
    abstract fun lastOnlineDao(): LastOnlineDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(applicationContext: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room
                        .databaseBuilder(applicationContext, AppDatabase::class.java, "instaDB.db")
                        // .createFromAsset("database/instaDB.db")
                        .build()
                }
            }
            return INSTANCE!!
        }
    }
}