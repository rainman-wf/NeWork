package ru.rainman.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.rainman.data.remote.response.PostResponse

internal interface MyWallApi {

    @GET("my/wall")
    suspend fun getAll(): List<PostResponse>

    @GET("my/wall/latest")
    suspend fun getLatest(@Query("count") count: Int): List<PostResponse>

    @GET("my/wall/{post_id}/after")
    suspend fun getAfter(@Path("post_id") id: Int, @Query("count") count: Int): List<PostResponse>

    @GET("my/wall/{post_id}/before")
    suspend fun getBefore(@Path("post_id") id: Int, @Query("count") count: Int): List<PostResponse>

    @GET("my/wall/{post_id}/newer")
    suspend fun getNewer(@Path("post_id") id: Int): List<PostResponse>
}