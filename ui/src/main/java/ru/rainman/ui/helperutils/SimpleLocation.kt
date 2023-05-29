package ru.rainman.ui.helperutils

import ru.rainman.domain.model.geo.Point
import java.io.Serializable

data class SimpleLocation(
    val point: Point,
    val name: String
) : Serializable
