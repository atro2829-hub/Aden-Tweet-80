package com.adentweets.app.util

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.adentweets.app.AdenTweetApp
import com.adentweets.app.AdenTweetApp.Companion.CHANNEL_LIKES
import com.adentweets.app.AdenTweetApp.Companion.CHANNEL_REPLIES
import com.adentweets.app.AdenTweetApp.Companion.CHANNEL_FOLLOWS
import com.adentweets.app.AdenTweetApp.Companion.CHANNEL_REPOSTS
import com.adentweets.app.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdenTweetFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val type = data["type"] ?: return
        val actorName = data["actorName"] ?: ""
        val actorAvatar = data["actorAvatar"] ?: ""
        val targetPostId = data["targetPostId"] ?: ""
        val targetPostText = data["targetPostText"] ?: ""
        val notifId = data["notifId"] ?: System.currentTimeMillis().toString()

        val (title, body, channel) = when (type) {
            "like" -> Triple(
                "$actorName liked your tweet",
                targetPostText,
                CHANNEL_LIKES
            )
            "follow" -> Triple(
                "$actorName followed you",
                "See their profile",
                CHANNEL_FOLLOWS
            )
            "reply" -> Triple(
                "$actorName replied to your tweet",
                targetPostText,
                CHANNEL_REPLIES
            )
            "repost" -> Triple(
                "$actorName reposted your tweet",
                targetPostText,
                CHANNEL_REPOSTS
            )
            "mention" -> Triple(
                "$actorName mentioned you",
                targetPostText,
                CHANNEL_REPLIES
            )
            else -> return
        }

        showNotification(notifId.toInt(), title, body, channel, targetPostId)
    }

    private fun showNotification(id: Int, title: String, body: String, channel: String, postId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("postId", postId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channel)
            .setSmallIcon(com.adentweets.app.R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(id, notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance("https://adentweet-default-rtdb.firebaseio.com")
            .getReference("users/$uid/fcmToken").setValue(token)
    }
}