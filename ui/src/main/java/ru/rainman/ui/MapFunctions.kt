package ru.rainman.cityquest.presentation.fragment.map

import android.content.Context
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView

class MapFunctions(
    private val mapView: MapView,
    private val context: Context
) {

    private val collection = mapView.map.mapObjects.addCollection()

    fun moveCamera(lat: Double, long: Double) {
        mapView.map.move(
            CameraPosition(
                Point(lat, long),
                17f,
                0f,
                0f
            )
        )
    }

//    fun moveCamera(markers: List<UserMarker>) {
//        val x0 = markers.maxOfOrNull { it.pointLat }
//        val x1 = markers.minOfOrNull { it.pointLat }
//        val y0 = markers.maxOfOrNull { it.pointLong }
//        val y1 = markers.minOfOrNull { it.pointLong }
//
//        val boundingBox = BoundingBox(Point(x0!!, y0!!), Point(x1!!, y1!!))
//
//        val cameraPosition = mapView.map.cameraPosition(boundingBox)
//
//        mapView.map.move(
//            CameraPosition(
//                cameraPosition.target,
//                cameraPosition.zoom - 0.9f,
//                0f,
//                0F
//            ),
//            Animation(Animation.Type.SMOOTH, 1f),
//            null
//        )
//    }
}