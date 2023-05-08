package ru.rainman.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import ru.rainman.ui.databinding.FragmentEventEditorBinding
import ru.rainman.ui.helperutils.TimeUnitsWrapper
import ru.rainman.ui.helperutils.toStringDate

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
    }
}


