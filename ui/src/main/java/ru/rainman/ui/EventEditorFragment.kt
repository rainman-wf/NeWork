package ru.rainman.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.common_utils.log
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.ui.databinding.FragmentEventEditorBinding
import ru.rainman.ui.helperutils.TimeUnitsWrapper
import ru.rainman.ui.helperutils.getClass
import ru.rainman.ui.helperutils.getNavController
import ru.rainman.ui.helperutils.toStringDate
import ru.rainman.ui.storage.StorageBottomSheet

@AndroidEntryPoint
class EventEditorFragment : Fragment(R.layout.fragment_event_editor) {

    private val binding: FragmentEventEditorBinding by viewBinding(FragmentEventEditorBinding::bind)
    private val viewModel: EventEditorViewModel by viewModels()
    private lateinit var navController: NavController

    private val datePicker =
        MaterialDatePicker.Builder.datePicker().setTitleText("Select date").setSelection(
            MaterialDatePicker.todayInUtcMilliseconds()
        ).build()

    private val timePicker =
        MaterialTimePicker.Builder().setTitleText("Set time").setTimeFormat(TimeFormat.CLOCK_24H)
            .build()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        navController =
            requireActivity().supportFragmentManager.getNavController(R.id.out_of_main_nav_host)

        binding.eventTypeToggle.addOnButtonCheckedListener { _, id, isChecked ->
            if (isChecked) viewModel.online(id == binding.eventEditorOnline.id)
        }

        viewModel.speakers.observe(viewLifecycleOwner) {
            binding.speakersChips.removeAllViews()
            it.forEach { user ->
                val chip = Chip(binding.speakersChips.context)
                chip.text = user.name
                chip.isCloseIconVisible = true
                binding.speakersChips.addView(chip)
            }
        }

        viewModel.selected.observe(viewLifecycleOwner) {
            binding.eventEditorInputLinkLayout.isSelected = it
            binding.eventEditorInputLinkLayout.isVisible = it
            binding.eventEditorInputGeoLayout.isSelected = !it
            binding.eventEditorInputGeoLayout.isVisible = !it
        }

        binding.eventEditorInputGeoLayout.setEndIconOnClickListener {
            viewModel.setLocation(null)
        }

        binding.eventEditorSetDate.setOnClickListener {
            datePicker.show(childFragmentManager, null)
        }

        binding.eventEditorInputGeo.setOnClickListener {
            navController.navigate(EventEditorFragmentDirections.actionEventEditorFragmentToMapFragment())
        }

        datePicker.addOnPositiveButtonClickListener {
            viewModel.setDate(it)
        }

        binding.eventEditorAppBar.setNavigationOnClickListener {
            navController.popBackStack()
        }

        binding.eventEditorSetTime.setOnClickListener {
            timePicker.show(childFragmentManager, null)
        }

        viewModel.date.observe(viewLifecycleOwner) {
            binding.eventEditorSetDate.setText(it?.toStringDate())
        }

        viewModel.time.observe(viewLifecycleOwner) {
            binding.eventEditorSetTime.setText(it.toString())
        }

        viewModel.location.observe(viewLifecycleOwner) {
            binding.eventEditorInputGeo.setText(it?.name)
        }

        timePicker.addOnPositiveButtonClickListener {
            viewModel.setTime(TimeUnitsWrapper(timePicker.hour, timePicker.minute))
        }

        setFragmentResultListener("select_users") { _, bundle ->
            viewModel.setSpeakers(bundle.getLongArray("ids")?.toList() ?: emptyList())

        }

        binding.addSpeaker.setOnClickListener {
            navController.navigate(
                EventEditorFragmentDirections
                    .actionEventEditorFragmentToSelectUsersDialogFragment(
                        viewModel.speakers.value?.map { it.id }?.toLongArray() ?: longArrayOf()
                    )
            )
        }

        setFragmentResultListener("media_data") {_ , bundle ->
            viewModel.setAttachment(bundle.getClass("media"))
        }

        binding.addEventAttachment.setOnClickListener {
            StorageBottomSheet().show(parentFragmentManager, "STORAGE")
        }

        setFragmentResultListener("geo_location") { _, bundle ->
            viewModel.setLocation(bundle.getClass("location"))
        }

        viewModel.attachment.observe(viewLifecycleOwner) {
            log(it)
        }
    }
}




