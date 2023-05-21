package ru.rainman.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.rainman.domain.model.geo.GeoObject
import ru.rainman.domain.model.geo.Geometry
import ru.rainman.domain.model.geo.Point

interface MapRepository  {

    val geoObjects: Flow<List<GeoObject>>

    suspend fun search(query: String, geometry: Geometry)
    suspend fun getGeocode(point: Point) : GeoObject?
    suspend fun getMyLocation() : Point
    suspend fun getGeoObjectData() : GeoObject
}