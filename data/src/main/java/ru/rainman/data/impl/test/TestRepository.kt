package ru.rainman.data.impl.test

import okhttp3.MediaType.Companion.parse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.rainman.data.remote.api.TgBotApi
import ru.rainman.domain.repository.ApiTestRepository
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestRepository @Inject constructor() : ApiTestRepository {

    private val baseUrl = "https://api.telegram.org/bot5168485531:AAE0sxjvdc6O1voIxvRwqs9klag5VSqIVbs/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(75, TimeUnit.SECONDS)
        .readTimeout(75, TimeUnit.SECONDS)
        .writeTimeout(75, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    private val requestSenderApi = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .baseUrl(baseUrl)
        .build().create(TgBotApi::class.java)

    override suspend fun sendPhoto(file: File) {
        val photo = MultipartBody.Part.createFormData(
            "photo", file.name, file.asRequestBody(
                "multipart/form-data".toMediaType()
            )
        )

        requestSenderApi.sendPhoto(1062747208L, photo)
    }

    override suspend fun sendVideo(file: File) {
        val video = MultipartBody.Part.createFormData(
            "video", file.name, file.asRequestBody(
                "multipart/form-data".toMediaType()
            )
        )

        requestSenderApi.sendVideo(1062747208L, video)
    }

    override suspend fun sendAudio(file: File) {
        val audio = MultipartBody.Part.createFormData(
            "audio", file.name, file.asRequestBody(
                "multipart/form-data".toMediaType()
            )
        )

        requestSenderApi.sendAudio(1062747208L, audio)
    }

}