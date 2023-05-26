package ru.rainman.ui

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.rainman.ui.databinding.DialogFragmentVideoPlayerBinding

class VideoPlayerDialogFragment : DialogFragment(R.layout.dialog_fragment_video_player) {

    private val binding: DialogFragmentVideoPlayerBinding
            by viewBinding(DialogFragmentVideoPlayerBinding::bind)

    private lateinit var player: ExoPlayer

    override fun onStart() {
        super.onStart()
        requireDialog().window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val source = requireArguments().getString("uri") ?: throw IllegalArgumentException("args missed")

        player = ExoPlayer.Builder(requireContext()).build()
        player.playWhenReady = true

        binding.playerView.player = player

        player.addMediaItem(MediaItem.fromUri(source))
        player.prepare()

        binding.close.setOnClickListener {
            (this as DialogFragment).dismiss()
        }

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        player.release()
    }
}