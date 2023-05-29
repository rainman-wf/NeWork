package ru.rainman.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.media3.common.MediaItem
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.rainman.ui.databinding.DialogFragmentVideoPlayerBinding

class VideoPlayerDialogFragment : DialogFragment(R.layout.dialog_fragment_video_player) {

    private val binding: DialogFragmentVideoPlayerBinding
            by viewBinding(DialogFragmentVideoPlayerBinding::bind)

    override fun onStart() {
        super.onStart()
        requireDialog().window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT) }
    }

    private val player = PlayerHolder.instance

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val source = requireArguments().getString("uri") ?: throw IllegalArgumentException("args missed")

        player.playWhenReady = true

        binding.playerView.player = player

        player.addMediaItem(MediaItem.fromUri(source))
        player.prepare()

        binding.close.setOnClickListener {
            player.stop()
            player.clearMediaItems()
            (this as DialogFragment).dismiss()
        }
    }
}