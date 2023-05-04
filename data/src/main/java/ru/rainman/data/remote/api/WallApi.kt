package ru.rainman.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.rainman.data.remote.response.PostResponse

internal interface WallApi {

    @GET("{author_id}/wall")
    suspend fun getAll(): Response<List<PostResponse>>

    @GET("{author_id}/wall/latest")
    suspend fun getLatest(@Path("author_id") id: Long, @Query("count") count: Int): Response<List<PostResponse>>

    @GET("{author_id}/wall/{post_id}/after")
    suspend fun getAfter(
        @Path("author_id") authorId: Long,
        @Path("post_id") postId: Long,
        @Query("count") count: Int
    ): Response<List<PostResponse>>


    @GET("{author_id}/wall/{post_id}/before")
    suspend fun getBefore(
        @Path("author_id") authorId: Long,
        @Path("post_id") postId: Long,
        @Query("count") count: Int
    ): Response<List<PostResponse>>

    @GET("{author_id}/wall/{post_id}/newer")
    suspend fun getNewer(
        @Path("author_id") authorId: Long,
        @Path("post_id") postId: Long
    ): Response<List<PostResponse>>
}
