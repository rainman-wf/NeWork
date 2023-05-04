package ru.rainman.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.rainman.data.remote.request.EventCreateRequest
import ru.rainman.data.remote.response.EventResponse

internal interface EventApi {

    @GET("events")
    suspend fun getAll() : Response<List<EventResponse>>

    @POST("events")
    suspend fun create(@Body eventCreateRequest: EventCreateRequest) : Response<EventResponse>

    @GET("events/latest")
    suspend fun getLatest(@Query("count") count: Int) : Response<List<EventResponse>>

    @GET("events/{event_id}")
    suspend fun getById(@Path("event_id") id: Long) : Response<EventResponse>

    @DELETE("events/{event_id}")
    suspend fun delete(@Path("event_id") id: Long) : Response<Unit>

    @GET("events/{event_id}/after")
    suspend fun getAfter(@Path("event_id") id: Long, @Query("count") count: Int) : Response<List<EventResponse>>

    @GET("events/{event_id}/before")
    suspend fun getBefore(@Path("event_id") id: Long, @Query("count") count: Int) : Response<List<EventResponse>>

    @POST("events/{event_id}/likes")
    suspend fun like(@Path("event_id") id: Long) : Response<EventResponse>

    @DELETE("events/{event_id}/likes")
    suspend fun unlike(@Path("event_id") id: Long): Response<EventResponse>

    @GET("events/{event_id}/newer")
    suspend fun getNewer(@Path("event_id") id: Long) : Response<List<EventResponse>>

    @POST("events/{event_id}/participants")
    suspend fun crateParticipant(@Path("event_id") id: Long) : Response<EventResponse>

    @DELETE("events/{event_id}/participants")
    suspend fun deleteParticipant(@Path("event_id") id: Long) : Response<EventResponse>
}