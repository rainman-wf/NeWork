package ru.rainman.data.impl.map

import com.example.common_utils.toGeometry
import com.example.common_utils.toModel
import com.example.common_utils.toPoint
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.runtime.Error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.rainman.domain.model.geo.*
import ru.rainman.domain.repository.MapRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class MapRepositoryImpl @Inject constructor(
    private val searchManager: SearchManager
) : MapRepository {

    override val geoObjects: MutableSharedFlow<List<GeoObject>> = MutableSharedFlow()

    override suspend fun search(query: String, geometry: Geometry) {
        searchManager.submit(
            query,
            geometry.toGeometry(),
            SearchOptions().setResultPageSize(5),
            object : Session.SearchListener {
                override fun onSearchResponse(p0: Response) {
                    CoroutineScope(Dispatchers.IO).launch {
                        geoObjects.emit(p0.collection.children.mapNotNull { item ->
                            item.obj?.toModel()
                        })
                    }
                }

                override fun onSearchError(p0: Error) {
                    throw RuntimeException("search request error")
                }
            }
        )
    }

    override suspend fun getGeocode(point: Point): GeoObject? = suspendCoroutine { continuation ->
        searchManager.submit(
            point.toPoint(),
            16,
            SearchOptions(),
            object : Session.SearchListener {
                override fun onSearchResponse(p0: Response) {
                    CoroutineScope(Dispatchers.IO).launch {
                        continuation.resume(p0.collection.children.firstOrNull()?.obj?.toModel())
                    }
                }

                override fun onSearchError(p0: Error) {
                    throw RuntimeException("search request error")
                }
            }
        )
    }

    override suspend fun getMyLocation(): Point {
        TODO("Not yet implemented")
    }

    override suspend fun getGeoObjectData(): GeoObject {
        TODO("Not yet implemented")
    }
}