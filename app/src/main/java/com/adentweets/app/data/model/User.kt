package com.adentweets.app.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val location: String = "",
    val website: String = "",
    val avatarUrl: String = "",
    val bannerUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val followersCount: Long = 0,
    val followingCount: Long = 0,
    val postsCount: Long = 0,
    val isVerified: Boolean = false,
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L,
    val settings: UserSettings = UserSettings()
)

data class UserSettings(
    val theme: String = "dark",
    val language: String = "ar",
    val pushNotifications: Boolean = true,
    val emailNotifications: Boolean = false,
    val notificationLikes: Boolean = true,
    val notificationReplies: Boolean = true,
    val notificationFollows: Boolean = true,
    val notificationMentions: Boolean = true,
    val notificationReposts: Boolean = true,
    val protectedTweets: Boolean = false,
    val allowMessageRequests: Boolean = true,
    val allowPhotoTagging: Boolean = true,
    val autoplayMedia: Boolean = true,
    val reduceMotion: Boolean = false,
    val highContrast: Boolean = false,
    val fontSize: String = "medium"
)

data class UserPreview(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val avatarUrl: String = "",
    val isVerified: Boolean = false,
    val isOnline: Boolean = false,
    val isFollowing: Boolean = false
)