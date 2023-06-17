package ru.rainman.nework.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.rainman.common.BASE_URL
import ru.rainman.data.remote.api.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideUserApi(client: OkHttpClient): UserApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(BASE_URL)
            .build()
            .create(UserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMediaApi(client: OkHttpClient): MediaApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(BASE_URL)
            .build()
            .create(MediaApi::class.java)
    }

    @Provides
    @Singleton
    fun provideJobsApi(client: OkHttpClient): JobApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(BASE_URL)
            .build()
            .create(JobApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMyWallApi(client: OkHttpClient): MyWallApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(BASE_URL)
            .build()
            .create(MyWallApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWallApi(client: OkHttpClient): WallApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(BASE_URL)
            .build()
            .create(WallApi::class.java)
    }

    @Provides
    @Singleton
    fun providePostsApi(client: OkHttpClient): PostApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(BASE_URL)
            .build()
            .create(PostApi::class.java)
    }

    @Provides
    @Singleton
    fun provideEventsApi(client: OkHttpClient): EventApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(BASE_URL)
            .build()
            .create(EventApi::class.java)
    }

}