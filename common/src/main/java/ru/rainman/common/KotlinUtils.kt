package ru.rainman.common

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun String.toDateTime(): LocalDateTime =
    LocalDateTime.parse(this.substring(0..18), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))