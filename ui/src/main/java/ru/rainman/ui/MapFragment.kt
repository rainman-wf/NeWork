package ru.rainman.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.common_utils.findPointOrNull
import com.example.common_utils.log
import com.example.common_utils.toModel
import com.example.common_utils.toPoint
import com.example.common_utils.toSearchResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.GeoObjectTapEvent
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.SearchFactory
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.cityquest.presentation.fragment.map.MapFunctions
import ru.rainman.domain.model.geo.GeoObject
import ru.rainman.domain.model.geo.SelectionMetaData
import ru.rainman.ui.databinding.FragmentMapBinding
import com.yandex.mapkit.map.Map


@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map), GeoObjectTapListener, InputListener {

    private val viewModel: MapViewModel by viewModels()

    private lateinit var mapView: MapView
    private lateinit var binding: FragmentMapBinding
    private lateinit var mapKit: MapKit

    private lateinit var collection: MapObjectCollection

    /*    private val args: MapFragmentArgs by navArgs()
    private val viewModel: MapViewModel by viewModels()*/

    private lateinit var mapFunctions: MapFunctions


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            binding.myLocation.isVisible = it
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.initialize(requireContext())
        SearchFactory.initialize(requireContext())
        super.onCreate(savedInstanceState)
        mapKit = MapKitFactory.getInstance()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentMapBinding.bind(view)

        mapView = binding.yaMap

        collection = mapView.map.mapObjects.addCollection()

        mapView.map.addTapListener(this)
        mapView.map.addInputListener(this)

        mapFunctions = MapFunctions(binding.yaMap, requireContext())

        val autocompleteSearchAdapter = AutocompleteSearchAdapter {
            mapView.map.move(CameraPosition(Point(it.latitude, it.longitude), 16f, 0f, 0f))
            viewModel.searchResulLiveData.postValue(emptyList())
        }

        binding.mapSearchAutocompleteLayout.searchSuggestsList.adapter = autocompleteSearchAdapter

        viewModel.searchResulLiveData.observe(viewLifecycleOwner) {
            autocompleteSearchAdapter.submitList(it.map(GeoObject::toSearchResult))
        }

        val userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true

        binding.mapSearchAutocompleteLayout.searchInputField.addTextChangedListener {
            viewModel.search(it.toString(), mapView.map.visibleRegion)
        }

        binding.myLocation.setOnClickListener {
            getMyLocation(object : PointCallBack {
                override fun callback(point: Point) {
                    mapFunctions.moveCamera(point.latitude, point.longitude)
                }
            })
        }

        viewModel.pointLiveData.observe(viewLifecycleOwner) {
            log(it)
        }
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

    private fun getMyLocation(callBack: PointCallBack) {
        if (
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            binding.myLocation.isVisible = true
            LocationServices.getFusedLocationProviderClient(requireContext())
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    callBack.callback(Point(loc.latitude, loc.longitude))
                }
        } else requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private interface PointCallBack {
        fun callback(point: Point)
    }

    override fun onObjectTap(p0: GeoObjectTapEvent): Boolean {
        val geoObject = p0.geoObject.toModel()
        log(geoObject)
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