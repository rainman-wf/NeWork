package ru.rainman.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.common_utils.findPointOrNull
import com.example.common_utils.log
import com.example.common_utils.toModel
import com.example.common_utils.toPoint
import com.example.common_utils.toSearchResult
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.GeoObjectTapEvent
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.SearchFactory
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.domain.model.geo.GeoObject
import ru.rainman.domain.model.geo.SelectionMetaData
import ru.rainman.ui.databinding.FragmentMapBinding
import com.yandex.mapkit.map.Map

@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map), GeoObjectTapListener, InputListener{

    private val viewModel: MapViewModel by viewModels()
    private val binding: FragmentMapBinding by viewBinding(FragmentMapBinding::bind)

    private lateinit var mapView: MapView
    private lateinit var mapKit: MapKit
    private lateinit var collection: MapObjectCollection

    /*    private val args: MapFragmentArgs by navArgs()
    private val viewModel: MapViewModel by viewModels()*/


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            binding.myLocation.isVisible = it
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.initialize(requireContext())
        SearchFactory.initialize(requireContext())
        super.onCreate(savedInstanceState)
        mapKit = MapKitFactory.getInstance()
        requestPermission()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.yaMap
        collection = mapView.map.mapObjects.addCollection()

        mapView.map.addTapListener(this)
        mapView.map.addInputListener(this)

        val autocompleteSearchAdapter = AutocompleteSearchAdapter {
            mapView.map.move(CameraPosition(Point(it.latitude, it.longitude), 16f, 0f, 0f))
            viewModel.resetSearchResults()
        }

        binding.mapSearchResults.adapter = autocompleteSearchAdapter

        viewModel.searchResulLiveData.observe(viewLifecycleOwner) {
            binding.mapSearchResults.isVisible = it.isNotEmpty()
            autocompleteSearchAdapter.submitList(
                it.map(GeoObject::toSearchResult).also { it1 -> log(it1) })
        }

        val userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true

        binding.mapSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) viewModel.resetSearchResults()
                else viewModel.search(newText.toString(), mapView.map.visibleRegion)
                return true
            }
        })

        binding.myLocation.setOnClickListener {
            userLocationLayer.cameraPosition()?.target?.let {
                mapView.map.move(
                    CameraPosition(it, 14f, 0f,0f),
                    Animation(Animation.Type.SMOOTH, 1f),
                    null
                )
            }

        }

        viewModel.pointLiveData.observe(viewLifecycleOwner) {
            log(it)
        }

        mapKit.createLocationManager().requestSingleUpdate(object : LocationListener {
            override fun onLocationUpdated(p0: Location) {
                mapView.map.move(
                    CameraPosition(p0.position, 14f, 0f,0f),
                    Animation(Animation.Type.SMOOTH, 1f),
                    null
                )
            }

            override fun onLocationStatusUpdated(p0: LocationStatus) {

            }

        })
    }

    override fun onStop() {
        binding.yaMap.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.yaMap.onStart()
    }

    private fun requestPermission() {
        if (
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onObjectTap(p0: GeoObjectTapEvent): Boolean {
        val geoObject = p0.geoObject.toModel()
        (geoObject.metadataContainer as? SelectionMetaData)?.let {
            mapView.map.selectGeoObject(it.id, it.layerId)
            geoObject.geometry.findPointOrNull().toPoint().let { point ->
                viewModel.getGeocode(point)
            }
        }
        return geoObject.metadataContainer is SelectionMetaData
    }

    override fun onMapTap(p0: Map, p1: Point) {
        viewModel.getGeocode(p1)
    }

    override fun onMapLongTap(p0: Map, p1: Point) {
        collection.clear()
        collection.addPlacemark(p1)
    }

}