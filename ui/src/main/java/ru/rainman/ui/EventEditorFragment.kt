package ru.rainman.ui

import android.os.Bundle
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.ui.databinding.FragmentEventEditorBinding
import ru.rainman.ui.helperutils.TimeUnitsWrapper
import ru.rainman.ui.helperutils.getNavController
import ru.rainman.ui.helperutils.log
import ru.rainman.ui.helperutils.toStringDate

@AndroidEntryPoint
class EventEditorFragment : Fragment(R.layout.fragment_event_editor) {

    private val binding: FragmentEventEditorBinding by viewBinding(FragmentEventEditorBinding::bind)
    private val viewModel: EventEditorViewModel by viewModels()

    private val datePicker =
        MaterialDatePicker.Builder.datePicker().setTitleText("Select date").setSelection(
            MaterialDatePicker.todayInUtcMilliseconds()
        ).build()

    private val timePicker =
        MaterialTimePicker.Builder().setTitleText("Set time").setTimeFormat(TimeFormat.CLOCK_24H)
            .build()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.eventTypeToggle.addOnButtonCheckedListener { _, id, isChecked ->
            if (isChecked) viewModel.online(id == binding.eventEditorOnline.id)
        }

        viewModel.speakers.observe(viewLifecycleOwner) {
            binding.speakersChips.removeAllViews()
            it.forEach {user ->
                val chip = Chip(binding.speakersChips.context)
                chip.text = user.name
                chip.isCloseIconVisible = true
                binding.speakersChips.addView(chip)
            }
        }

        viewModel.selected.observe(viewLifecycleOwner) {
            binding.eventEditorInputLinkLayout.isEnabled = it
            binding.eventEditorInputGeoLayout.isEnabled = !it
        }

        binding.eventEditorSetDate.setOnClickListener {
            datePicker.show(childFragmentManager, null)
        }

        datePicker.addOnPositiveButtonClickListener {
            viewModel.setDate(it)
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

        timePicker.addOnPositiveButtonClickListener {
            viewModel.setTime(TimeUnitsWrapper(timePicker.hour, timePicker.minute))
        }

        setFragmentResultListener("select_users") { _, bundle ->
            viewModel.setSpeakers(bundle.getLongArray("ids")?.toList() ?: emptyList())
        }

        binding.addSpeaker.setOnClickListener {
            requireActivity().supportFragmentManager.getNavController(R.id.out_of_main_nav_host)
                .navigate(
                    EventEditorFragmentDirections.actionEventEditorFragmentToSelectUsersDialogFragment(
                        viewModel.speakers.value?.map { it.id }?.toLongArray() ?: longArrayOf()
                    )
                )
        }

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.mediaStorageConatiner)

        binding.addEventAttachment.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }
}

