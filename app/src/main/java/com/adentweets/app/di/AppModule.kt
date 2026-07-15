package com.adentweets.app.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.adentweets.app.data.remote.FirebaseService
import com.adentweets.app.data.repository.AuthRepositoryImpl
import com.adentweets.app.data.repository.PostRepositoryImpl
import com.adentweets.app.data.repository.UserRepositoryImpl
import com.adentweets.app.data.repository.MessageRepositoryImpl
import com.adentweets.app.data.repository.NotificationRepositoryImpl
import com.adentweets.app.data.repository.SearchRepositoryImpl
import com.adentweets.app.data.repository.ListRepositoryImpl
import com.adentweets.app.domain.repository.AuthRepository
import com.adentweets.app.domain.repository.PostRepository
import com.adentweets.app.domain.repository.UserRepository
import com.adentweets.app.domain.repository.MessageRepository
import com.adentweets.app.domain.repository.NotificationRepository
import com.adentweets.app.domain.repository.SearchRepository
import com.adentweets.app.domain.repository.ListRepository
import com.adentweets.app.util.NetworkMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        val db = FirebaseDatabase.getInstance("https://adentweet-default-rtdb.firebaseio.com")
        db.setPersistenceEnabled(true)
        return db
    }

    @Provides
    @Singleton
    fun provideFirebaseService(
        auth: FirebaseAuth,
        database: FirebaseDatabase
    ): FirebaseService = FirebaseService(auth, database)

    @Provides
    @Singleton
    fun provideNetworkMonitor(): NetworkMonitor = NetworkMonitor()

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseService: FirebaseService): AuthRepository =
        AuthRepositoryImpl(firebaseService)

    @Provides
    @Singleton
    fun providePostRepository(firebaseService: FirebaseService): PostRepository =
        PostRepositoryImpl(firebaseService)

    @Provides
    @Singleton
    fun provideUserRepository(firebaseService: FirebaseService): UserRepository =
        UserRepositoryImpl(firebaseService)

    @Provides
    @Singleton
    fun provideMessageRepository(firebaseService: FirebaseService): MessageRepository =
        MessageRepositoryImpl(firebaseService)

    @Provides
    @Singleton
    fun provideNotificationRepository(firebaseService: FirebaseService): NotificationRepository =
        NotificationRepositoryImpl(firebaseService)

    @Provides
    @Singleton
    fun provideSearchRepository(firebaseService: FirebaseService): SearchRepository =
        SearchRepositoryImpl(firebaseService)

    @Provides
    @Singleton
    fun provideListRepository(firebaseService: FirebaseService): ListRepository =
        ListRepositoryImpl(firebaseService)
}