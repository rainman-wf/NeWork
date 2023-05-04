package ru.rainman.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.rainman.data.remote.response.JobResponse
import ru.rainman.domain.model.Job

internal interface JobApi {

    @GET("my/jobs")
    suspend fun getAll() : Response<List<JobResponse>>

    @POST("my/jobs")
    suspend fun create(@Body job: Job) : Response<JobResponse>

    @DELETE("my/jobs/job_id")
    suspend fun delete(@Path("job_id") id: Long)

    @GET("{user_id}/jobs")
    suspend fun getUserJobs(@Path("user_id") id: Long) : Response<List<JobResponse>>
}