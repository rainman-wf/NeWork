package ru.rainman.nework.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.rainman.data.impl.event.EventRepositoryImpl
import ru.rainman.data.impl.job.JobRepositoryImpl
import ru.rainman.data.impl.map.MapRepositoryImpl
import ru.rainman.data.impl.post.PostRepositoryImpl
import ru.rainman.data.impl.user.UserRepositoryImpl
import ru.rainman.domain.repository.EventRepository
import ru.rainman.domain.repository.JobRepository
import ru.rainman.domain.repository.MapRepository
import ru.rainman.domain.repository.PostRepository
import ru.rainman.domain.repository.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindEventRepository(impl: EventRepositoryImpl) : EventRepository

    @Binds
    @Singleton
    fun bindPostRepository(impl: PostRepositoryImpl) : PostRepository

    @Binds
    @Singleton
    fun bindMapRepository(impl: MapRepositoryImpl) : MapRepository

    @Binds
    @Singleton
    fun bindUserRepository(impl: UserRepositoryImpl) : UserRepository


    @Binds
    @Singleton
    fun bindJobRepository(impl: JobRepositoryImpl) : JobRepository
}