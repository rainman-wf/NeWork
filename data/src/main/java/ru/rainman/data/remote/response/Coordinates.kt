package ru.rainman.data.remote.response

import com.google.gson.annotations.SerializedName

data class Coordinates(
    @SerializedName("lat")
    val latitude: String,
    @SerializedName("long")
    val longitude: String
)