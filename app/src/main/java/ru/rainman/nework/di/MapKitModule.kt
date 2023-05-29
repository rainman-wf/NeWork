package ru.rainman.nework.di


import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManagerType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MapKitModule {

    @Provides
    fun provideSearchManager() =
        SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE)
}