package ru.rainman.ui

import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.domain.dto.NewAttachmentDto
import ru.rainman.domain.dto.NewEventDto
import ru.rainman.domain.model.Attachment
import ru.rainman.domain.model.Coordinates
import ru.rainman.domain.model.EventType
import ru.rainman.ui.EventEditorViewModel.PublishingState.*
import ru.rainman.ui.databinding.FragmentEventEditorBinding
import ru.rainman.ui.helperutils.PubType
import ru.rainman.ui.helperutils.TimeUnitsWrapper
import ru.rainman.ui.helperutils.args.ArgKey
import ru.rainman.ui.helperutils.args.RequestKey
import ru.rainman.ui.helperutils.args.putString
import ru.rainman.ui.helperutils.args.setResultListener
import ru.rainman.ui.helperutils.getObject
import ru.rainman.ui.helperutils.getNavController
import ru.rainman.ui.helperutils.snack
import ru.rainman.ui.helperutils.toStringDate
import ru.rainman.ui.helperutils.toUploadMedia
import ru.rainman.ui.storage.StorageBottomSheet
import ru.rainman.ui.view.SpeakerChip
import java.time.Instant
import java.time.LocalDateTime
import java.util.TimeZone

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

        binding.saveEvent.setOnClickListener {

            val content = binding.inputEventContent.text

            if (content.isNullOrBlank()) {
                snack("CONTENT IS REQUIRED")
                return@setOnClickListener
            }

            val date = viewModel.date.value

            if (date == null) {
                snack("DATE IS REQUIRED ")
                return@setOnClickListener
            }

            val time = viewModel.time.value

            if (time == null) {
                snack("TIME IS REQUIRED ")
                return@setOnClickListener
            }

            val localDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(date),
                TimeZone.getDefault().toZoneId()
            )

            val isOnline = viewModel.isOnline.value!!

            val link = binding.eventEditorInputLink.text
            val geo = viewModel.location.value?.let {
                Coordinates(it.point.latitude, it.point.longitude)
            }

            if (isOnline) {

                if (link.isNullOrBlank()) {
                    snack("LINK IS REQUIRED FOR ONLINE MODE")
                    return@setOnClickListener
                }
            } else {
                if (geo == null) {
                    snack("LOCATION IS REQUIRED FOR OFFLINE MODE")
                    return@setOnClickListener
                }
            }

            val dateTime =
                localDate.plusHours(time.hours.toLong()).plusMinutes(time.minutes.toLong())

            val dto = NewEventDto(
                content = requireNotNull(content.toString()),
                dateTime = dateTime,
                speakerIds = viewModel.speakers.value?.map { it.id } ?: listOf(),
                coordinates = if (!isOnline) geo else null,
                link = if (isOnline) link.toString() else null,
                type = if (isOnline) EventType.ONLINE else EventType.OFFLINE,
                attachment = viewModel.attachment.value?.let {
                    NewAttachmentDto(
                        type = when (it) {
                            is Attachment.Image -> NewAttachmentDto.Type.IMAGE
                            is Attachment.Video -> NewAttachmentDto.Type.VIDEO
                            is Attachment.Audio -> NewAttachmentDto.Type.AUDIO
                        },
                        media = it.uri.toUri().toUploadMedia(requireContext())
                    )
                }
            )

            viewModel.publish(dto)
        }


        viewModel.eventStatus.observe(viewLifecycleOwner) {
            when (it) {
                ERROR -> snack("SENDING ERROR")
                LOADING -> snack("SENDING...")
                SUCCESS -> navController.navigateUp()
                null -> {}
            }
        }

        viewModel.speakers.observe(viewLifecycleOwner) {

            binding.speakersChips.removeAllViews()

            it.forEach { user ->
                val chip = SpeakerChip(binding.speakersChips.context)
                chip.setIconUrl(user.avatar)
                chip.text = user.name
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    viewModel.removeSpeaker(user.id)
                }
                binding.speakersChips.addView(chip)
            }
        }

        viewModel.isOnline.observe(viewLifecycleOwner) {
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


        binding.addSpeaker.setOnClickListener {
            navController.navigate(
                EventEditorFragmentDirections
                    .actionEventEditorFragmentToSelectUsersDialogFragment(
                        viewModel.speakers.value?.map { it.id }?.toLongArray() ?: longArrayOf(),
                        editableType = PubType.EVENT
                    )
            )
        }

        setResultListener(RequestKey.EVENT_REQUEST_KEY_SPEAKERS) { bundle ->
            viewModel.setSpeakers(bundle.getLongArray(ArgKey.USERS.name)?.toList() ?: emptyList())
        }

        setResultListener(RequestKey.EVENT_REQUEST_KEY_ATTACHMENT) { bundle ->
            viewModel.setAttachment(bundle.getObject(ArgKey.ATTACHMENT.name))
        }

        setResultListener(RequestKey.EVENT_REQUEST_KEY_LOCATION) { bundle ->
            viewModel.setLocation(bundle.getObject(ArgKey.LOCATION.name))
        }


        binding.addEventAttachment.setOnClickListener {
            val dialog = StorageBottomSheet()
            val bundle = Bundle()
            bundle.putString(ArgKey.ATTACHMENT, PubType.EVENT.name)
            dialog.arguments = bundle
            StorageBottomSheet().show(parentFragmentManager, "STORAGE")
        }

        viewModel.attachment.observe(viewLifecycleOwner) { attachment ->

            binding.attachmentPreview.isVisible = attachment != null

            attachment?.let { binding.attachmentPreview.setData(it) }
                ?: binding.attachmentPreview.recycle()
            binding.attachmentPreview.isVisible = attachment != null
        }
    }

}




