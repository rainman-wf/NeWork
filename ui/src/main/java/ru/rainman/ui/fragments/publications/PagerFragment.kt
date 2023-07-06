package ru.rainman.ui.fragments.publications

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.rainman.ui.R
import ru.rainman.ui.databinding.FragmentPagerBinding
import ru.rainman.ui.fragments.MainFragmentDirections
import ru.rainman.ui.fragments.publications.event.EventsFragment
import ru.rainman.ui.fragments.publications.post.PostsFragment
import ru.rainman.ui.helperutils.PlayerHolder
import ru.rainman.ui.helperutils.getNavController
import java.lang.RuntimeException

@AndroidEntryPoint
class PagerFragment : Fragment(R.layout.fragment_pager) {

    private lateinit var binding: FragmentPagerBinding
    private val viewModel: PublicationsPagerViewModel by viewModels()
    private val player = PlayerHolder.instance

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding = FragmentPagerBinding.bind(view)

        PlayerHolder.currentPlayedItem.asLiveData().observe(viewLifecycleOwner) {
            binding.audioPlayerController.isVisible = it != null
            lifecycleScope.launch {
                PlayerHolder.setCurrentPlayedItem(it)
            }
        }

        viewModel.isLoggedIn.observe(viewLifecycleOwner) {
            binding.add.isVisible = it
        }

        PlayerHolder.listen(object : PlayerHolder.PlayerListener {

            override fun stateChangedListener(
                duration: Long,
                position: Long,
                playerStateReady: Boolean
            ) {
                lifecycleScope.launch {
                    while (playerStateReady) {
                        binding.audioProgress.setProgress(
                            calculateProgress(
                                duration,
                                position
                            ), true
                        )
                        delay(100)
                    }
                }
            }

            override fun isPlayingChanged(isPlaying: Boolean) {
                binding.playPause.isSelected = isPlaying
                PlayerHolder.setIsPlaying(isPlaying)
            }
        })

        binding.stopAudio.setOnClickListener {
            PlayerHolder.stopAudio()
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

        val fragments = listOf(
            PostsFragment.newInstance(-1),
                EventsFragment()
            )

        pager.adapter = PagerAdapter(childFragmentManager, lifecycle, fragments)

        binding.add.setOnClickListener {
            requireActivity().supportFragmentManager.getNavController(R.id.out_of_main_nav_host)
                .navigate(
                    when (pager.currentItem) {
                        0 -> MainFragmentDirections.actionMainFragmentToPostEditorFragment()
                        1 -> MainFragmentDirections.actionMainFragmentToEventEditorFragment()
                        else -> throw RuntimeException("invalid item")
                    }
                )
        }

        TabLayoutMediator(tabs, pager) { tab, pos ->
            tab.text = when (pos) {
                0 ->  "Posts"
                1 -> "Events"
                else -> null
            }

        }.attach()
    }

    private fun calculateProgress(duration: Long, position: Long): Int {
        return (position.toDouble() / duration.toDouble() * 100).toInt()
    }

}
