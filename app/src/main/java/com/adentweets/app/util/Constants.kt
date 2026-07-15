package com.adentweets.app.util

object Constants {
    const val TWEET_MAX_LENGTH = 280
    const val BIO_MAX_LENGTH = 160
    const val NAME_MAX_LENGTH = 50
    const val USERNAME_MIN_LENGTH = 3
    const val USERNAME_MAX_LENGTH = 15
    const val PASSWORD_MIN_LENGTH = 8
    const val MAX_MEDIA_ATTACHMENTS = 4
    const val POSTS_PAGE_SIZE = 20
    const val USERS_PAGE_SIZE = 20
    const val MESSAGES_PAGE_SIZE = 30
    const val FOLLOW_DEBOUNCE_MS = 2000L
    const val LIKE_DEBOUNCE_MS = 500L
    const val SEARCH_DEBOUNCE_MS = 300L
    const val TYPING_DEBOUNCE_MS = 2000L
    const val NOTIFICATION_BADGE_MAX = 99

    const val DB_USERS = "users"
    const val DB_POSTS = "posts"
    const val DB_CONVERSATIONS = "conversations"
    const val DB_NOTIFICATIONS = "notifications"

    object Routes {
        const val SPLASH = "splash"
        const val WELCOME = "welcome"
        const val LOGIN = "login"
        const val REGISTER = "register"
        const val FORGOT_PASSWORD = "forgot_password"
        const val HOME = "home"
        const val EXPLORE = "explore"
        const val SEARCH_RESULTS = "search_results"
        const val NOTIFICATIONS = "notifications"
        const val MESSAGES = "messages"
        const val CHAT = "chat/{conversationId}"
        const val NEW_MESSAGE = "new_message"
        const val COMPOSE = "compose"
        const val TWEET_DETAIL = "tweet/{postId}"
        const val PROFILE = "profile"
        const val USER_PROFILE = "user_profile/{userId}"
        const val EDIT_PROFILE = "edit_profile"
        const val FOLLOWERS = "followers/{userId}"
        const val FOLLOWING = "following/{userId}"
        const val BOOKMARKS = "bookmarks"
        const val LISTS = "lists"
        const val LIST_DETAIL = "list/{listId}"
        const val CREATE_LIST = "create_list"
        const val IMAGE_VIEWER = "image_viewer"
        const val VIDEO_PLAYER = "video_player"
        const val SETTINGS = "settings"
        const val ACCOUNT_SETTINGS = "account_settings"
        const val PRIVACY_SETTINGS = "privacy_settings"
        const val NOTIF_SETTINGS = "notif_settings"
        const val APPEARANCE_SETTINGS = "appearance_settings"
        const val ABOUT = "about"
        const val TRENDING_DETAIL = "trending/{topic}"

        fun chat(conversationId: String) = "chat/$conversationId"
        fun tweetDetail(postId: String) = "tweet/$postId"
        fun userProfile(userId: String) = "user_profile/$userId"
        fun followers(userId: String) = "followers/$userId"
        fun following(userId: String) = "following/$userId"
        fun listDetail(listId: String) = "list/$listId"
        fun trending(topic: String) = "trending/$topic"
    }
}