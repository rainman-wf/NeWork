package ru.rainman.nework.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.rainman.data.local.AppDb
import ru.rainman.nework.APP_SETTINGS_SHARED_PREFS_NAME
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDataSourceModule {
    @Provides
    @Singleton
    fun provideSharedPrefs(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(APP_SETTINGS_SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context) : AppDb {
        return Room.databaseBuilder(context, AppDb::class.java, "app.db").build()
    }
}