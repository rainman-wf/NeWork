package ru.rainman.nework.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.rainman.data.impl.event.EventRepositoryImpl
import ru.rainman.data.impl.test.TestRepository
import ru.rainman.domain.repository.ApiTestRepository
import ru.rainman.domain.repository.EventRepository
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
}