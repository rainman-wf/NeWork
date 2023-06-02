package ru.rainman.ui

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.rainman.nework.ui.users.UserListArg
import ru.rainman.ui.helperutils.Args.*
import ru.rainman.ui.databinding.FragmentPagerBinding
import ru.rainman.ui.helperutils.CurrentPlayedItemState
import ru.rainman.ui.helperutils.PlayerHolder
import ru.rainman.ui.helperutils.PubType

@AndroidEntryPoint
class PagerFragment : Fragment(R.layout.fragment_pager) {

    private lateinit var binding: FragmentPagerBinding
    private val args: PagerFragmentArgs by navArgs()
    private val viewModel: PublicationsPagerViewModel by viewModels()
    private val player = PlayerHolder.instance
    private val _currentPlayedItem = MutableStateFlow<CurrentPlayedItemState?>(null)
    val currentPlayedItem: StateFlow<CurrentPlayedItemState?> get() = _currentPlayedItem

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding = FragmentPagerBinding.bind(view)

        viewModel.currentPlayedItemState.observe(viewLifecycleOwner) {
            binding.audioPlayerController.isVisible = it != null
            lifecycleScope.launch {
                _currentPlayedItem.emit(it)
            }
        }


        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                (playbackState == Player.STATE_READY).let {

                    lifecycleScope.launch {
                            while (it) {
                                binding.audioProgress.setProgress(
                                    calculateProgress(
                                        player.duration,
                                        player.currentPosition
                                    ), true
                                )
                                delay(100)
                            }
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                binding.playPause.isSelected = isPlaying

                viewModel.setIsPlaying(isPlaying)
            }
        })

        binding.stopAudio.setOnClickListener {
            stopAudio()
        }

        binding.playPause.setOnClickListener {
            if (player.isPlaying) player.pause() else player.play()
        }

        binding.audioProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            private var mProcess = 0

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) mProcess = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                seekBar.progress = mProcess
                player.seekTo(player.duration * mProcess / 100)
            }
        })

        val tabs = binding.tabs
        val pager = binding.pager

        val fragments =
            when (args.intention) {
                PUBLICATIONS -> listOf(
                    PostsFragment(),
                    EventsFragment()
                )

                USERS -> listOf(
                    UsersFragment.newInstance(UserListArg.ALL),
                    UsersFragment.newInstance(UserListArg.FAVORITE)
                )
            }

        pager.adapter = PagerAdapter(childFragmentManager, lifecycle, fragments)

        TabLayoutMediator(tabs, pager) { tab, pos ->
            tab.text = when (pos) {
                0 -> when (args.intention) {
                    PUBLICATIONS -> "Posts"
                    USERS -> "All"
                }

                1 -> when (args.intention) {
                    PUBLICATIONS -> "Events"
                    USERS -> "Favorite"
                }

                else -> null
            }
        }.attach()
    }

    private fun calculateProgress(duration: Long, position: Long): Int {
        return (position.toDouble() / duration.toDouble() * 100).toInt()
    }

    fun playAudio(url: String, type: PubType, pubId: Long) {
        viewModel.currentPlayedItemState.value?.let {
            if (it.type == type && it.id == pubId) {
                if (it.isPlaying) player.pause()
                else player.play()
            } else playNew(url, type, pubId)

        } ?: playNew(url, type, pubId)
    }

    private fun playNew(url: String, type: PubType, pubId: Long) {
        player.stop()
        player.clearMediaItems()
        viewModel.setCurrentPlayedItem(type, pubId)
        player.addMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.play()
    }

    fun stopAudio() {
        player.stop()
        viewModel.clearCurrentPlayedItem()
        player.clearMediaItems()
    }

}
