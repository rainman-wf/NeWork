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
import ru.rainman.common.APP_SETTINGS_SHARED_PREFS_NAME
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
        val db = Room.databaseBuilder(context, AppDb::class.java, "app.db").build()
        db.openHelper.readableDatabase.query(
            """
CREATE TRIGGER IF NOT EXISTS delete_post_attachment AFTER DELETE ON posts
BEGIN
DELETE FROM attachments WHERE key = OLD.attachment_key;
END;
CREATE TRIGGER IF NOT EXISTS delete_event_attachment AFTER DELETE ON events
BEGIN
DELETE FROM attachments WHERE key = OLD.attachment_key;
END;
CREATE TRIGGER IF NOT EXISTS delete_post_link AFTER DELETE ON posts
BEGIN
DELETE FROM links WHERE key = OLD.link_key;
END;
CREATE TRIGGER IF NOT EXISTS delete_event_links AFTER DELETE ON events
BEGIN
DELETE FROM links WHERE key = OLD.link_key;
END;
CREATE TRIGGER IF NOT EXISTS reset_attachment AFTER DELETE ON attachments
BEGIN
UPDATE posts SET attachment_key = NULL WHERE attachment_key = OLD.key;
UPDATE events SET attachment_key = NULL WHERE attachment_key = OLD.key;
END;
CREATE TRIGGER IF NOT EXISTS reset_link AFTER DELETE ON links
BEGIN
UPDATE posts SET link_key = NULL WHERE link_key = OLD.key;
UPDATE events SET link_key = NULL WHERE link_key = OLD.key;
END;
"""
        )
        return db
    }
}