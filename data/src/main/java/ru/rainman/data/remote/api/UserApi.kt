package ru.rainman.data.remote.api

import ru.rainman.data.remote.request.AuthenticationRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.rainman.data.remote.response.UserResponse
import ru.rainman.domain.model.Token

interface UserApi {

    @GET("users")
    suspend fun getAll() : Response<List<UserResponse>>

    @POST("users/authentication")
    suspend fun singIn(@Body authenticationRequest: AuthenticationRequest) : Response<Token>

    @Multipart
    @POST("users/registration")
    suspend fun register(
        @Part("login") login: RequestBody,
        @Part("password") password: RequestBody,
        @Part("name") name: RequestBody,
        @Part file: MultipartBody.Part?,
    ): Response<Token>

    @GET("users/{user_id}")
    suspend fun getById(@Path("user_id") id: Long) : Response<UserResponse>
}