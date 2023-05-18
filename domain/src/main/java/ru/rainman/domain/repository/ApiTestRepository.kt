package ru.rainman.domain.repository

import java.io.File

interface ApiTestRepository {
    suspend fun sendPhoto(file: File)
    suspend fun sendVideo(file: File)
    suspend fun sendAudio(file: File)
}