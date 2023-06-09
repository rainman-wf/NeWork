package ru.rainman.data.remote.api

import ru.rainman.data.remote.request.PostCreateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.rainman.data.remote.response.PostResponse

interface PostApi {

    @GET("posts")
    suspend fun getAll() : Response<List<PostResponse>>

    @POST("posts")
    suspend fun create(@Body postCreateRequest: PostCreateRequest) : Response<PostResponse>

    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count: Int) : Response<List<PostResponse>>

    @GET("posts/{post_id}")
    suspend fun getById(@Path("post_id") id: Long) : Response<PostResponse>

    @DELETE("posts/{post_id}")
    suspend fun delete(@Path("post_id") id: Long) : Response<Unit>

    @GET("posts/{post_id}/after")
    suspend fun getAfter(@Path("post_id") id: Long, @Query("count") count: Int) : Response<List<PostResponse>>

    @GET("posts/{post_id}/before")
    suspend fun getBefore(@Path("post_id") id: Long, @Query("count") count: Int) : Response<List<PostResponse>>

    @POST("posts/{post_id}/likes")
    suspend fun like(@Path("post_id") id: Long) : Response<PostResponse>

    @DELETE("posts/{post_id}/likes")
    suspend fun unlike(@Path("post_id") id: Long) : Response<PostResponse>

    @GET("posts/newer")
    suspend fun getNewer(@Query("count") count: Long) : Response<List<PostResponse>>
}