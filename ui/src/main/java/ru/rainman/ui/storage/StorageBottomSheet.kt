package ru.rainman.ui.storage

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import ru.rainman.domain.model.Attachment
import ru.rainman.ui.PagerAdapter
import ru.rainman.ui.R
import ru.rainman.ui.databinding.BottomSheetStorageBinding
import ru.rainman.ui.helperutils.MediaType

class StorageBottomSheet : BottomSheetDialogFragment(R.layout.bottom_sheet_storage) {

    private lateinit var binding: BottomSheetStorageBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = BottomSheetStorageBinding.bind(view)

        val tabs = binding.tabs
        val pagerView = binding.pager

        val fragments = listOf(
            ImageStorageFragment(),
            VideoStorageFragment(),
            AudioStorageFragment()
        )

        pagerView.adapter = PagerAdapter(childFragmentManager, lifecycle, fragments)

        TabLayoutMediator(tabs, pagerView) {tab, pos ->
            tab.text = when(pos) {
                0 -> "Images"
                1 -> "Video"
                2 -> "Audio"
                else -> null
            }
        }.attach()
    }

    fun setUri(uri: Uri, type: MediaType) {
        val bundle = Bundle()
        bundle.putString("uri", uri.toString())
        bundle.putString("type", type.name)
        setFragmentResult("media_uri", bundle)
        dismiss()
    }

}


