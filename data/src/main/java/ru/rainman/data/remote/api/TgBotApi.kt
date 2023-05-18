package ru.rainman.data.remote.api

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface TgBotApi {

    @Multipart
    @POST("sendPhoto")
    suspend fun sendPhoto(
        @Query("chat_id") chatId: Long,
        @Part photo: MultipartBody.Part,
    )

    @Multipart
    @POST("sendAudio")
    suspend fun sendAudio(
        @Query("chat_id") chatId: Long,
        @Part audio: MultipartBody.Part,
    )

    @Multipart
    @POST("sendVideo")
    suspend fun sendVideo(
        @Query("chat_id") chatId: Long,
        @Part audio: MultipartBody.Part,
    )
}