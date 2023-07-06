package ru.rainman.ui.storage

import android.os.Bundle
import android.view.View

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import ru.rainman.domain.model.Attachment
import ru.rainman.ui.fragments.publications.PagerAdapter
import ru.rainman.ui.R
import ru.rainman.ui.databinding.BottomSheetStorageBinding
import ru.rainman.ui.helperutils.PubType
import ru.rainman.ui.helperutils.args.ArgKey
import ru.rainman.ui.helperutils.args.RequestKey
import ru.rainman.ui.helperutils.args.getString
import ru.rainman.ui.helperutils.args.putObject
import ru.rainman.ui.helperutils.args.putResult
import ru.rainman.ui.storage.abstractions.AttachmentSingleEvent


class StorageBottomSheet : BottomSheetDialogFragment(R.layout.bottom_sheet_storage) {

    private lateinit var binding: BottomSheetStorageBinding

    private val editableType: PubType by lazy {
        val arg = arguments?.getString(ArgKey.ATTACHMENT) ?: throw NullPointerException("args missed")
        PubType.valueOf(arg)
    }

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

        TabLayoutMediator(tabs, pagerView) { tab, pos ->
            tab.text = when (pos) {
                0 -> "Images"
                1 -> "Video"
                2 -> "Audio"
                else -> null
            }
        }.attach()

        AttachmentSingleEvent.value.observe(viewLifecycleOwner) {
            it?.let { att -> setUri(att) }
        }

    }

    private fun setUri(attachment: Attachment) {
        val bundle = Bundle()
        bundle.putObject(ArgKey.ATTACHMENT, attachment)
        when (editableType) {
            PubType.POST -> putResult(RequestKey.POST_REQUEST_KEY_ATTACHMENT, bundle)
            PubType.EVENT -> putResult(RequestKey.EVENT_REQUEST_KEY_ATTACHMENT, bundle)
        }
        dismiss()
    }

}


