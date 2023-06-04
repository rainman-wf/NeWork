package ru.rainman.ui

import ru.rainman.domain.model.geo.Point as PointModel
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.common_utils.findPointOrNull
import com.example.common_utils.log
import com.example.common_utils.toModel
import com.example.common_utils.toPoint
import com.example.common_utils.toSearchResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
import com.yandex.mapkit.user_location.UserLocationLayer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.rainman.domain.model.geo.ToponymObjectData
import ru.rainman.ui.helperutils.SimpleLocation
import ru.rainman.ui.helperutils.args.ArgKey
import ru.rainman.ui.helperutils.args.putObject
import ru.rainman.ui.helperutils.args.putResult
import ru.rainman.ui.helperutils.getNavController
//import ru.rainman.ui.storage.args.ArgKeys
//import ru.rainman.ui.storage.args.RequestKey

@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map), GeoObjectTapListener, InputListener {

    private val viewModel: MapViewModel by viewModels()
    private val binding: FragmentMapBinding by viewBinding(FragmentMapBinding::bind)

    private lateinit var mapView: MapView
    private lateinit var mapKit: MapKit
    private lateinit var collection: MapObjectCollection

    private val cameraLocation = MutableSharedFlow<PointModel>()
    private lateinit var userLocationLayer: UserLocationLayer

    /*    private val args: MapFragmentArgs by navArgs()
    private val viewModel: MapViewModel by viewModels()*/

    private val locationListener = object : LocationListener {
        override fun onLocationUpdated(p0: Location) {
            lifecycleScope.launch {
                cameraLocation.emit(p0.position.let { PointModel(it.latitude, it.longitude) })
            }
        }

        override fun onLocationStatusUpdated(p0: LocationStatus) {}

    }

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

    private fun getMyLocation() {
        val context = requireContext()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            LocationServices.getFusedLocationProviderClient(requireContext())
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener {
                    lifecycleScope.launch {
                        cameraLocation.emit(PointModel(it.latitude, it.longitude))
                    }
                }
                .addOnFailureListener {
                    mapKit.createLocationManager().requestSingleUpdate(locationListener)
                }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController =
            requireActivity().supportFragmentManager.getNavController(R.id.out_of_main_nav_host)

        mapView = binding.yaMap
        collection = mapView.map.mapObjects.addCollection()

        mapView.map.addTapListener(this)
        mapView.map.addInputListener(this)

        val autocompleteSearchAdapter = AutocompleteSearchAdapter {
            lifecycleScope.launch {
                cameraLocation.emit(it)
            }
            viewModel.resetSearchResults()
        }

        binding.mapSearchResults.adapter = autocompleteSearchAdapter

        viewModel.searchResulLiveData.observe(viewLifecycleOwner) {
            binding.mapSearchResults.isVisible = it.isNotEmpty()
            autocompleteSearchAdapter.submitList(
                it.map(GeoObject::toSearchResult).also { it1 -> log("search $it1") })
        }

        userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true

        getMyLocation()

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

        binding.mapsToolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }

        binding.done.setOnClickListener {
            val bundle = Bundle()
            viewModel.pointLiveData.value?.let {
                val point = it.geometry[0] as PointModel
                SimpleLocation(
                    PointModel(
                        latitude = point.latitude,
                        longitude = point.longitude
                    ),
                    it.name ?: (it.metadataContainer as ToponymObjectData).address.formattedAddress
                )
            }?.let {
                bundle.putObject(ArgKey.LOCATION, it)
                putResult(ru.rainman.ui.helperutils.args.RequestKey.EVENT_REQUEST_KEY_LOCATION, bundle)
            }

            navController.navigateUp()
        }

        binding.myLocation.setOnClickListener {
            getMyLocation()
        }

        viewModel.pointLiveData.observe(viewLifecycleOwner) {
            log("observe $it")
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        lifecycleScope.launch {
            cameraLocation.collectLatest {
                mapView.map.move(
                    CameraPosition(Point(it.latitude, it.longitude), 17f, 0f, 0f),
                    Animation(Animation.Type.SMOOTH, 1f),
                    null
                )
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)

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
        log("tap $geoObject")
        (geoObject.metadataContainer as? SelectionMetaData)?.let {
            mapView.map.selectGeoObject(it.id, it.layerId)
            geoObject.geometry.findPointOrNull().toPoint().let { point ->
                viewModel.getGeocode(point)
                collection.clear()
                collection.addPlacemark(point)
            }
        }
        return geoObject.metadataContainer is SelectionMetaData
    }

    override fun onMapTap(p0: Map, p1: Point) {}

    override fun onMapLongTap(p0: Map, p1: Point) {
        collection.clear()
        collection.addPlacemark(p1)
    }

}