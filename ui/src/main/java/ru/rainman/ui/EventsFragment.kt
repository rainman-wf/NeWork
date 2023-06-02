package ru.rainman.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.rainman.domain.model.Attachment
import ru.rainman.ui.databinding.FragmentEventsBinding
import ru.rainman.ui.helperutils.PubType
import ru.rainman.ui.helperutils.getNavController
import ru.rainman.ui.helperutils.showVideoDialog
import ru.rainman.ui.helperutils.snack

@AndroidEntryPoint
class EventsFragment : Fragment(R.layout.fragment_events) {

    private val binding: FragmentEventsBinding by viewBinding(FragmentEventsBinding::bind)
    private lateinit var navController: NavController
    private val viewModel: EventsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        navController =
            requireActivity().supportFragmentManager.getNavController(R.id.out_of_main_nav_host)

        binding.newEvent.setOnClickListener {
            navController.navigate(MainFragmentDirections.actionMainFragmentToEventEditorFragment())
        }

        val parentFragment = requireParentFragment() as PagerFragment

        val adapter =
            EventsAdapter(parentFragment.currentPlayedItem, object : OnEventClickListener {
                override fun onLikeClicked(eventId: Long) {
                    viewModel.like(eventId)
                }

                override fun onParticipateClicked(eventId: Long) {
                    viewModel.participate(eventId)
                }

                override fun onShareClicked(eventId: Long) {
                    snack("share $eventId")
                }

                override fun onMoreClicked(eventId: Long) {
                    snack("more $eventId")
                }

                override fun onAuthorClicked(eventId: Long) {
                    snack("author $eventId")
                }

                override fun onEventClicked(eventId: Long) {
                    snack("event $eventId")
                }

                override fun onPlayClicked(postId: Long, attachment: Attachment) {
                    when (attachment) {
                        is Attachment.Video -> {
                            parentFragment.stopAudio()
                            showVideoDialog(attachment.uri)
                        }
                        is Attachment.Audio -> parentFragment.playAudio(attachment.uri, PubType.EVENT, postId)
                        else -> {}
                    }
                }

            })

        binding.eventList.adapter = adapter

        lifecycleScope.launch {
            viewModel.events.collectLatest {
                adapter.submitData(it)
            }
        }
    }
}


