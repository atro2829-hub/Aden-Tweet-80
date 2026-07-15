package com.adentweets.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AdenTweetApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseDatabase.getInstance("https://adentweet-default-rtdb.firebaseio.com").setPersistenceEnabled(true)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_MESSAGES, "Messages",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Direct message notifications" },
                NotificationChannel(
                    CHANNEL_LIKES, "Likes",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Like notifications" },
                NotificationChannel(
                    CHANNEL_FOLLOWS, "Follows",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Follow notifications" },
                NotificationChannel(
                    CHANNEL_REPLIES, "Replies",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Reply notifications" },
                NotificationChannel(
                    CHANNEL_REPOSTS, "Reposts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Repost notifications" }
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannels(channels)
        }
    }

    companion object {
        const val CHANNEL_MESSAGES = "aden_messages"
        const val CHANNEL_LIKES = "aden_likes"
        const val CHANNEL_FOLLOWS = "aden_follows"
        const val CHANNEL_REPLIES = "aden_replies"
        const val CHANNEL_REPOSTS = "aden_reposts"
    }
}