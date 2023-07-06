package ru.rainman.ui.fragments.publications.event

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
import ru.rainman.ui.R
import ru.rainman.ui.databinding.FragmentEventsBinding
import ru.rainman.ui.fragments.MainFragmentDirections
import ru.rainman.ui.fragments.users.UsersDialogFragment
import ru.rainman.ui.helperutils.PlayerHolder
import ru.rainman.ui.helperutils.PubType
import ru.rainman.ui.helperutils.getNavController
import ru.rainman.ui.helperutils.showUsersDialog
import ru.rainman.ui.helperutils.showVideoDialog
import ru.rainman.ui.helperutils.snack
import ru.rainman.ui.helperutils.states.Error
import ru.rainman.ui.helperutils.states.Loading
import ru.rainman.ui.helperutils.states.Success

@AndroidEntryPoint
class EventsFragment : Fragment(R.layout.fragment_events) {

    private val binding: FragmentEventsBinding by viewBinding(FragmentEventsBinding::bind)
    private lateinit var navController: NavController
    private val viewModel: EventsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        navController =
            requireActivity().supportFragmentManager.getNavController(R.id.out_of_main_nav_host)

        viewModel.interaction.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    is Error -> snack(it.message)
                    Loading -> {}
                    Success -> {}
                }
            }
        }

        val adapter =
            EventListAdapter(PlayerHolder.currentPlayedItem, object : OnEventClickListener {
                override fun onLikeClicked(id: Long) {
                    viewModel.like(id)
                }

                override fun onLikesCountClicked(ids: List<Long>) {
                    val dialog = UsersDialogFragment()
                    val bundle = Bundle()
                    bundle.putString("intention", "Like Owners")
                    bundle.putLongArray("ids", ids.toLongArray())
                    dialog.arguments = bundle
                    dialog.show(parentFragmentManager, null)
                }

                override fun onParticipateClicked(eventId: Long) {
                    viewModel.participate(eventId)
                }

                override fun onParticipantsCountClicked(ids: List<Long>) {
                    showUsersDialog("Participants", ids)
                }

                override fun onShareClicked(id: Long) {
                    snack("share $id")
                }

                override fun onEditClicked(id: Long) {
                    navController.navigate(
                        MainFragmentDirections.actionMainFragmentToEventEditorFragment(
                            id
                        )
                    )
                }

                override fun onDeleteClicked(id: Long) {
                    viewModel.delete(id)
                }

                override fun onAuthorClicked(id: Long) {
                    navController.navigate(
                        MainFragmentDirections.actionMainFragmentToPostsFragment(
                            id
                        )
                    )
                }

                override fun onBodyClicked(id: Long) {
                    navController.navigate(
                        MainFragmentDirections.actionMainFragmentToEventDetailsFragment(
                            id
                        )
                    )
                }

                override fun onPlayClicked(id: Long, attachment: Attachment) {
                    when (attachment) {
                        is Attachment.Video -> {
                            PlayerHolder.stopAudio()
                            showVideoDialog(attachment.uri, attachment.ratio)
                        }

                        is Attachment.Audio -> PlayerHolder.playAudio(
                            attachment.uri,
                            PubType.EVENT,
                            id
                        )

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


