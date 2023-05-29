package ru.rainman.ui.storage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.loader.app.LoaderManager
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.ui.R
import ru.rainman.ui.databinding.FragmentStorageListBinding
import ru.rainman.ui.helperutils.MediaType

@AndroidEntryPoint
class VideoStorageFragment : Fragment(R.layout.fragment_storage_list) {

    private val viewModel: VideoStorageViewModel by viewModels()
    private lateinit var binding: FragmentStorageListBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentStorageListBinding.bind(view)
        binding.storageGrid.layoutManager = GridLayoutManager(requireContext(), 3)

        LoaderManager
            .getInstance(this)
            .initLoader(
                1,
                null,
                VideoLoaderManager(viewModel, requireContext())
            )

        val adapter = VideoStorageAdapter {
            (requireParentFragment() as StorageBottomSheet).setUri(it, MediaType.VIDEO)
        }

        binding.storageGrid.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }
}



