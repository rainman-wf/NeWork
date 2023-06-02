package ru.rainman.ui.storage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.loader.app.LoaderManager
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.ui.R
import ru.rainman.ui.databinding.FragmentStorageListBinding
import ru.rainman.ui.storage.abstractions.AttachmentSingleEvent

@AndroidEntryPoint
class AudioStorageFragment : Fragment(R.layout.fragment_storage_list) {


    private val viewModel: AudioStorageViewModel by viewModels()
    private lateinit var binding: FragmentStorageListBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentStorageListBinding.bind(view)
        binding.storageGrid.layoutManager = LinearLayoutManager(requireContext())

        LoaderManager
            .getInstance(this)
            .initLoader(
                1,
                null,
                AudioLoaderManager(viewModel, requireContext())
            )

        val adapter = AudioStorageAdapter {
            AttachmentSingleEvent.value.postValue(it)
        }

        binding.storageGrid.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

}
