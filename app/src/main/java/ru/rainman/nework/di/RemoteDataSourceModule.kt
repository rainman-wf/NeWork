package ru.rainman.nework.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.rainman.data.local.pref.AppAuth
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteDataSourceModule {

    @Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideClient(logging: HttpLoggingInterceptor, appAuth: AppAuth): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                appAuth.tokenValue?.let { token ->
                    val newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", token.token)
                        .build()
                    return@addInterceptor chain.proceed(newRequest)
                }

                chain.proceed(chain.request())
            }
            .build()
    }
}