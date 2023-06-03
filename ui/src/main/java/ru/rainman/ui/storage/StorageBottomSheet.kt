package ru.rainman.ui.storage

import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.setFragmentResult
import com.example.common_utils.log
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import ru.rainman.domain.model.Attachment
import ru.rainman.ui.PagerAdapter
import ru.rainman.ui.R
import ru.rainman.ui.databinding.BottomSheetStorageBinding
import ru.rainman.ui.helperutils.PubType
import ru.rainman.ui.helperutils.toUploadMedia
import ru.rainman.ui.storage.abstractions.AttachmentSingleEvent
import ru.rainman.ui.storage.args.ArgKeys
import ru.rainman.ui.storage.args.RequestKey

class StorageBottomSheet : BottomSheetDialogFragment(R.layout.bottom_sheet_storage) {

    private lateinit var binding: BottomSheetStorageBinding

    private val editableType: PubType by lazy {
        val arg = arguments?.getString(ArgKeys.ATTACHMENT.name) ?: throw NullPointerException("args missed")
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
            it?.let { att ->
                log("file name : ${att.uri.toUri().toUploadMedia(requireContext()).fileName}")
                log("uri : ${att.uri}")
                setUri(att)
            }
        }

    }

    private fun setUri(attachment: Attachment) {
        val bundle = Bundle()
        bundle.putSerializable(ArgKeys.ATTACHMENT.name, attachment)
        when (editableType) {
            PubType.POST -> setFragmentResult(RequestKey.POST_REQUEST_KEY_ATTACHMENT.name, bundle)
            PubType.EVENT -> setFragmentResult(RequestKey.EVENT_REQUEST_KEY_ATTACHMENT.name, bundle)
        }
        dismiss()
    }

}


