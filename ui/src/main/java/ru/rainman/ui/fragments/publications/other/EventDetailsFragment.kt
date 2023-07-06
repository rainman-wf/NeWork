package ru.rainman.ui.fragments.publications.other

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.SearchFactory
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.common.log
import ru.rainman.domain.model.Event
import ru.rainman.domain.model.EventType
import ru.rainman.ui.R
import ru.rainman.ui.databinding.FragmentEventDetailsBinding
import ru.rainman.ui.helperutils.SimpleLocation
import ru.rainman.ui.helperutils.getNavController
import ru.rainman.ui.helperutils.represent
import ru.rainman.ui.view.SpeakerChip

@AndroidEntryPoint
class EventDetailsFragment : Fragment(R.layout.fragment_event_details) {

    private val viewModel: EventDetailsViewModel by viewModels()
    private val args: EventDetailsFragmentArgs by navArgs()
    private val binding: FragmentEventDetailsBinding by viewBinding(FragmentEventDetailsBinding::bind)


    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.initialize(requireContext())
        SearchFactory.initialize(requireContext())
        super.onCreate(savedInstanceState)
    }

    override fun onStop() {
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val eventId = args.eventId

        viewModel.getEvent(eventId)

        val navController = requireActivity().supportFragmentManager.getNavController(R.id.out_of_main_nav_host)

        viewModel.pointLiveData.observe(viewLifecycleOwner) {
            it?.let { geo ->
                binding.location.represent(geo)
                binding.location.root.setOnClickListener {
                    navController.navigate(EventDetailsFragmentDirections.actionEventDetailsFragmentToEventLocationMapFragment(
                        SimpleLocation(geo.geometry.first() as ru.rainman.domain.model.geo.Point, geo.name ?: "")
                    ))
                }
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }

        viewModel.event.observe(viewLifecycleOwner) {

            it.speakerIds.forEach { user ->
                val chip = SpeakerChip(binding.speakersChips.context)
                chip.setIconUrl(user.avatar)
                chip.text = user.name
                binding.speakersChips.addView(chip)
            }

            binding.toolbar.subtitle = it.datetime.toString()

            when {
                it.correctOnline -> {
                    it.link!!.log()
                    binding.link.represent(it.link!!)
                }
                it.correctOffline -> {
                    viewModel.getGeocode(Point(it.coordinates!!.latitude, it.coordinates!!.longitude))
                }
                else -> binding.invalidEventDataErrorText.isVisible = true
            }

            binding.attachment.isVisible = it.attachment != null

            binding.content.text = it.content

            it.attachment?.let { it1 -> binding.attachment.setData(it1) }
        }
    }

    private val Event.correctOnline: Boolean get() = type == EventType.ONLINE && link != null
    private val Event.correctOffline: Boolean get() = type == EventType.OFFLINE && coordinates != null
}




