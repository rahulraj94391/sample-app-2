package com.example.instagram.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.instagram.data.dao.BlockDao
import com.example.instagram.data.dao.CacheDao
import com.example.instagram.data.dao.ChatsDao
import com.example.instagram.data.dao.CommentDao
import com.example.instagram.data.dao.FollowDao
import com.example.instagram.data.dao.HashTagDao
import com.example.instagram.data.dao.LastOnlineDao
import com.example.instagram.data.dao.LikesDao
import com.example.instagram.data.dao.LocationCacheDao
import com.example.instagram.data.dao.LocationDao
import com.example.instagram.data.dao.LoginCredDao
import com.example.instagram.data.dao.PostDao
import com.example.instagram.data.dao.PostTextDao
import com.example.instagram.data.dao.ProfileDao
import com.example.instagram.data.dao.RecentSearchDao
import com.example.instagram.data.dao.SavedPostDao
import com.example.instagram.data.dao.SearchDao
import com.example.instagram.data.dao.TagPeopleDao
import com.example.instagram.data.entity.BlockedUsers
import com.example.instagram.data.entity.Chat
import com.example.instagram.data.entity.Comment
import com.example.instagram.data.entity.Follow
import com.example.instagram.data.entity.HashTag
import com.example.instagram.data.entity.ImageCache
import com.example.instagram.data.entity.LastOnline
import com.example.instagram.data.entity.Likes
import com.example.instagram.data.entity.Location
import com.example.instagram.data.entity.LocationCache
import com.example.instagram.data.entity.LoginCred
import com.example.instagram.data.entity.Post
import com.example.instagram.data.entity.PostImage
import com.example.instagram.data.entity.PostText
import com.example.instagram.data.entity.Profile
import com.example.instagram.data.entity.RecentSearch
import com.example.instagram.data.entity.SavedPost
import com.example.instagram.data.entity.Tag

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
        LastOnline::class,
        Location::class,
        BlockedUsers::class,
        HashTag::class,
        LocationCache::class
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
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun lastOnlineDao(): LastOnlineDao
    abstract fun locationDao(): LocationDao
    abstract fun blockDao(): BlockDao
    abstract fun hashtagDao(): HashTagDao
    abstract fun locationCacheDao(): LocationCacheDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(applicationContext: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "instaDB.db")
                        .createFromAsset("database/instaDB.db")
                        .build()
                }
            }
            return INSTANCE!!
        }
    }
}