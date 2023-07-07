package ru.rainman.ui.fragments.users

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.ui.R
import ru.rainman.ui.databinding.DialogFragmentJobEditorBinding
import ru.rainman.ui.helperutils.snack
import ru.rainman.ui.helperutils.states.Error
import ru.rainman.ui.helperutils.states.Loading
import ru.rainman.ui.helperutils.states.Success
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class JobEditorDialogFragment : DialogFragment(R.layout.dialog_fragment_job_editor) {

    lateinit var binding: DialogFragmentJobEditorBinding
    private val viewModel: JobEditorViewModel by viewModels()
    private lateinit var args: Bundle

    private val startDatePicker =
        MaterialDatePicker.Builder.datePicker().setTitleText("Select start date").setSelection(
            MaterialDatePicker.todayInUtcMilliseconds()
        ).build()

    private val finishDatePicker =
        MaterialDatePicker.Builder.datePicker().setTitleText("Select finish date").setSelection(
            MaterialDatePicker.todayInUtcMilliseconds()
        ).build()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogFragmentJobEditorBinding.inflate(requireActivity().layoutInflater)
        args = arguments ?: throw NullPointerException("${this::class.simpleName} : args missed")

        val builder = MaterialAlertDialogBuilder(requireContext())

        args.getLong("userId").let { if (it > 0) viewModel.loadJob(it) }


        binding.startDate.setOnClickListener {
            startDatePicker.show(childFragmentManager, null)
        }

        binding.endDate.setOnClickListener {
            finishDatePicker.show(childFragmentManager, null)
        }

        startDatePicker.addOnPositiveButtonClickListener {
            viewModel.setStart(it)
        }

        finishDatePicker.addOnPositiveButtonClickListener {
            viewModel.setFinish(it)
        }

        builder.apply {
            setView(binding.root)
            setTitle("Job editor")
        }

        binding.buttonPositive.setOnClickListener {
            viewModel.save(
                name = binding.organisationName.text.toString(),
                position = binding.position.text.toString(),
                link = binding.link.text?.toString()
            )
        }

        binding.buttonNegative.setOnClickListener {
            dismiss()
        }

        return builder.create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.sendingState.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    is Error -> snack(it.message)
                    Loading -> snack("Sending...")
                    Success -> dismiss()
                }
            }
        }

        viewModel.oldName.observe(viewLifecycleOwner) {
            binding.organisationName.setText(it)
        }

        viewModel.oldPosition.observe(viewLifecycleOwner) {
            binding.position.setText(it)
        }

        viewModel.oldLink.observe(viewLifecycleOwner) {
            binding.link.setText(it)
        }

        viewModel.job.observe(viewLifecycleOwner) {
            binding.startDate.setText(it.start.format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
            it.finish?.let { date ->
                binding.endDate.setText(date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
            }
        }
    }
}