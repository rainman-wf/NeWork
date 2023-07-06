package ru.rainman.ui.fragments.map


import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.SearchFactory
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.ui.R
import ru.rainman.ui.databinding.FragmentEventLocationBinding
import ru.rainman.ui.helperutils.getNavController

@AndroidEntryPoint
class EventLocationMapFragment : Fragment(R.layout.fragment_event_location) {

    private val binding: FragmentEventLocationBinding by viewBinding(FragmentEventLocationBinding::bind)

    private lateinit var mapView: MapView
    private lateinit var mapKit: MapKit
    private lateinit var collection: MapObjectCollection
    private val args: EventLocationMapFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.initialize(requireContext())
        SearchFactory.initialize(requireContext())
        super.onCreate(savedInstanceState)
        mapKit = MapKitFactory.getInstance()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController =
            requireActivity().supportFragmentManager.getNavController(R.id.out_of_main_nav_host)

        val point = Point(args.point.point.latitude, args.point.point.longitude)

        mapView = binding.yaMap
        collection = mapView.map.mapObjects.addCollection()

        collection.addPlacemark(point)
        binding.mapsToolbar.subtitle = args.point.name

        mapView.map.move(
            CameraPosition(point, 17f, 0f, 0f),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )

        binding.mapsToolbar.setNavigationOnClickListener {
            navController.navigateUp()
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
}