package ru.rainman.common

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

fun String.toDateTime(): LocalDateTime =
    LocalDateTime.parse(this.substring(0..18), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))

fun LocalDate.toLocalDateTime(): LocalDateTime =
    LocalDateTime.of(this, LocalTime.of(0, 0))