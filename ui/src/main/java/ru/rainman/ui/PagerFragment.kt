package ru.rainman.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.common_utils.log
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import ru.rainman.ui.Args.*
import ru.rainman.ui.databinding.FragmentPagerBinding

@AndroidEntryPoint
class PagerFragment : Fragment(R.layout.fragment_pager) {

    private val binding: FragmentPagerBinding by viewBinding(FragmentPagerBinding::bind)
    private val args: PagerFragmentArgs by navArgs()
    private lateinit var player: ExoPlayer
    private val viewModel: PublicationsPagerViewModel by viewModels()

    private val _currentPlayedItem = MutableSharedFlow<CurrentPlayedItemState?>()
    val currentPlayedItem: SharedFlow<CurrentPlayedItemState?> get() = _currentPlayedItem

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        player = ExoPlayer.Builder(requireContext()).build()

        viewModel.currentPlayedItemState.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                _currentPlayedItem.emit(it)
            }
        }


        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                (playbackState == Player.STATE_READY).let {

                    binding.audioPlayerController.isVisible = it

                    lifecycleScope.launch {
                        while (it) {
                            binding.audioProgress.progress =
                                calculateProgress(player.duration, player.currentPosition)
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
            player.stop()
        }

        binding.playPause.setOnClickListener {
            if (player.isPlaying) player.pause() else player.play()
        }

        val tabs = binding.tabs
        val pager = binding.pager

        val fragments =
            when (args.intention) {
                PUBLICATIONS -> listOf(
                    PostsFragment(),
                    EventsFragment()
                )

                USERS -> listOf(
                    UsersFragment(),
                    UsersFragment()
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

        log("input params: $type, $pubId")

        log("State before: ${viewModel.currentPlayedItemState.value}")

        viewModel.currentPlayedItemState.value?.let {
            if (it.type == type && it.id == pubId) {
                if (it.isPlaying) player.pause()
                else player.play()
            } else playNew(url, type, pubId)

        } ?: playNew(url, type, pubId)

        log("State after: ${viewModel.currentPlayedItemState.value}")
    }

    private fun playNew(url: String, type: PubType, pubId: Long) {
        viewModel.setCurrentPlayedItem(type, pubId)
        player.stop()
        player.clearMediaItems()
        player.addMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.play()
    }

    fun stopAudio() {
        player.stop()
        viewModel.clearCurrentPlayedItem()
        player.clearMediaItems()
    }

    override fun onStop() {
        super.onStop()
        player.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

}
