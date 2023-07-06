package ru.rainman.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.loader.app.LoaderManager
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.rainman.ui.R
import ru.rainman.ui.databinding.BottomSheetImagesBinding
import ru.rainman.ui.storage.ImageLoaderManager
import ru.rainman.ui.storage.ImageStorageAdapter
import ru.rainman.ui.storage.ImageStorageViewModel

class SelectAvatarBottomSheetDialog : BottomSheetDialogFragment(R.layout.bottom_sheet_images) {

    private lateinit var binding: BottomSheetImagesBinding
    private val viewModel: ImageStorageViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding = BottomSheetImagesBinding.bind(view)
        binding.imageGrid.storageGrid.layoutManager = GridLayoutManager(requireContext(), 3)

        LoaderManager
            .getInstance(this)
            .initLoader(
                2,
                null,
                ImageLoaderManager(viewModel, requireContext())
            )


        val adapter = ImageStorageAdapter {
            val bundle = Bundle()
            bundle.putString("uri", it.uri)
            setFragmentResult("REGISTRATION_AVATAR", bundle)
            dismiss()
        }

        binding.imageGrid.storageGrid.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

}