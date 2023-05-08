package ru.rainman.nework.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.rainman.data.local.AppDb
import ru.rainman.data.local.dao.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {

    @Provides
    @Singleton
    fun provideUserDao(db: AppDb) : UserDao = db.userDao

    @Provides
    @Singleton
    fun provideEventDao(db: AppDb) : EventDao = db.eventDao

    @Provides
    @Singleton
    fun provideJobDao(db: AppDb) : JobDao = db.jobDao

    @Provides
    @Singleton
    fun providePostDao(db: AppDb) : PostDao = db.postDao

    @Provides
    @Singleton
    fun provideRemoteKeysDao(db: AppDb) : RemoteKeyDao = db.remoteKeyDao
}