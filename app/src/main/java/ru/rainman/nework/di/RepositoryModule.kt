package ru.rainman.nework.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.rainman.data.impl.event.EventRepositoryImpl
import ru.rainman.data.impl.map.MapRepositoryImpl
import ru.rainman.data.impl.post.PostRepositoryImpl
import ru.rainman.data.impl.test.TestRepository
import ru.rainman.domain.repository.ApiTestRepository
import ru.rainman.domain.repository.EventRepository
import ru.rainman.domain.repository.MapRepository
import ru.rainman.domain.repository.PostRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindTestRepository(impl: TestRepository) : ApiTestRepository


    @Binds
    @Singleton
    fun bindEventRepository(impl: EventRepositoryImpl) : EventRepository

    @Binds
    @Singleton
    fun bindPostRepository(impl: PostRepositoryImpl) : PostRepository

    @Binds
    @Singleton
    fun bindMapRepository(impl: MapRepositoryImpl) : MapRepository
}