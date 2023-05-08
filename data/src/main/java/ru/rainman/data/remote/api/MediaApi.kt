package ru.rainman.data.remote.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import ru.rainman.data.remote.response.MediaResponse

interface MediaApi {

    @Multipart
    @POST("media")
    suspend fun uploadMedia(@Part media: MultipartBody.Part): Response<MediaResponse>

}