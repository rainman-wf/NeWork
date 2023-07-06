package ru.rainman.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query
import ru.rainman.data.BuildConfig.MAPS_API_KEY

interface YaMapStaticApi {

    @GET("/")
    fun getLocationPreview(
        @Query ("ll") ll: String,
        @Query ("pt") point: String,
        @Query ("key") key: String = MAPS_API_KEY,
        @Query ("z") zoom: Int = 10,
        @Query ("l") l: String = "map",
        @Query ("size") size: String = "128,128"
    )
}