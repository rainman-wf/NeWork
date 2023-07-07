package ru.rainman.ui.fragments.publications.other

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.domain.model.EventType
import ru.rainman.ui.R
import ru.rainman.ui.databinding.FragmentEventEditorBinding
import ru.rainman.ui.helperutils.PubType
import ru.rainman.ui.helperutils.args.ArgKey
import ru.rainman.ui.helperutils.args.RequestKey
import ru.rainman.ui.helperutils.args.putString
import ru.rainman.ui.helperutils.args.setResultListener
import ru.rainman.ui.helperutils.getObject
import ru.rainman.ui.helperutils.getNavController
import ru.rainman.ui.helperutils.snack
import ru.rainman.ui.helperutils.states.Error
import ru.rainman.ui.helperutils.states.Loading
import ru.rainman.ui.helperutils.states.Success
import ru.rainman.ui.storage.StorageBottomSheet
import ru.rainman.ui.view.SpeakerChip
import java.time.format.DateTimeFormatter
import kotlin.properties.Delegates

@AndroidEntryPoint
class EventEditorFragment : Fragment(R.layout.fragment_event_editor) {

    private val binding: FragmentEventEditorBinding by viewBinding(FragmentEventEditorBinding::bind)
    private val viewModel: EventEditorViewModel by viewModels()
    private lateinit var navController: NavController
    private val args: EventEditorFragmentArgs by navArgs()

    private val datePicker =
        MaterialDatePicker.Builder.datePicker().setTitleText("Select date").setSelection(
            MaterialDatePicker.todayInUtcMilliseconds()
        ).build()

    private val timePicker =
        MaterialTimePicker.Builder().setTitleText("Set time").setTimeFormat(TimeFormat.CLOCK_24H)
            .build()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            showStorageDialog(PubType.EVENT)
        }

    private var storagePermissionGranted by Delegates.notNull<Boolean>()

    private fun requestPermission() {
        if (!storagePermissionGranted)
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storagePermissionGranted = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        navController =
            requireActivity().supportFragmentManager.getNavController(R.id.out_of_main_nav_host)

        if (args.eventId > 0) viewModel.loadEvent(args.eventId)

        binding.eventTypeToggle.addOnButtonCheckedListener { _, id, isChecked ->
            if (isChecked) viewModel.online(id == binding.eventEditorOnline.id)
        }

        binding.saveEvent.setOnClickListener {
            viewModel.publish(binding.inputEventContent.text.toString(), requireContext())
        }


        viewModel.eventStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Error -> snack(it.message)
                Loading -> snack("SENDING...")
                Success -> navController.navigateUp()
                null -> {}
            }
        }

        viewModel.oldContent.observe(viewLifecycleOwner) {
            binding.inputEventContent.setText(it)
        }

        viewModel.editableEvent.observe(viewLifecycleOwner) {

            binding.speakersChips.removeAllViews()

            it.speakers.forEach { user ->
                val chip = SpeakerChip(binding.speakersChips.context)
                chip.setIconUrl(user.avatar)
                chip.text = user.name
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    viewModel.removeSpeaker(user.id)
                }
                binding.speakersChips.addView(chip)
            }

            (it.type == EventType.ONLINE).let { online ->
                binding.eventEditorInputLinkLayout.isSelected = online
                binding.eventEditorInputLinkLayout.isVisible = online
                binding.eventEditorInputGeoLayout.isSelected = !online
                binding.eventEditorInputGeoLayout.isVisible = !online
            }

            binding.eventEditorSetDate.setText(it.dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
            binding.eventEditorSetTime.setText(it.dateTime.format(DateTimeFormatter.ofPattern("HH:mm")))

            binding.attachmentPreview.isVisible = it.attachment != null

            it.attachment
                ?.let { att ->
                    binding.attachmentPreview.setData(att)
                    binding.addEventAttachment.text = "Clear attachment"
                    binding.addEventAttachment.icon = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.close, null)
                    binding.addEventAttachment.setOnClickListener {
                        viewModel.setAttachment(null)
                    }
                } ?: kotlin.run {
                binding.addEventAttachment.text = "Add Attachment"
                binding.addEventAttachment.icon = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.add, null)
                binding.attachmentPreview.recycle()

                binding.addEventAttachment.setOnClickListener {
                    if (storagePermissionGranted) showStorageDialog(PubType.EVENT) else requestPermission()
                }
            }
        }

        viewModel.locationName.observe(viewLifecycleOwner) {
            binding.eventEditorInputGeo.setText(it ?: "Undefined location")
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


        timePicker.addOnPositiveButtonClickListener {
            viewModel.setTime(timePicker.hour, timePicker.minute)
        }


        binding.addSpeaker.setOnClickListener {
            navController.navigate(
                EventEditorFragmentDirections.actionEventEditorFragmentToSelectUsersDialogFragment(
                    viewModel.editableEvent.value?.speakers?.map { it.id }?.toLongArray()
                        ?: longArrayOf(),
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

    }
}

fun Fragment.showStorageDialog(pubType: PubType) {
    val dialog = StorageBottomSheet()
    val bundle = Bundle()
    bundle.putString(ArgKey.ATTACHMENT, pubType.name)
    dialog.arguments = bundle
    dialog.show(parentFragmentManager, "STORAGE")
}




