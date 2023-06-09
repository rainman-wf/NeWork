package ru.rainman.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.domain.model.geo.Point
import ru.rainman.ui.databinding.FragmentPostEditorBinding
import ru.rainman.ui.helperutils.PubType
import ru.rainman.ui.helperutils.Status
import ru.rainman.ui.helperutils.args.ArgKey
import ru.rainman.ui.helperutils.args.RequestKey
import ru.rainman.ui.helperutils.args.putString
import ru.rainman.ui.helperutils.args.setResultListener
import ru.rainman.ui.helperutils.getNavController
import ru.rainman.ui.helperutils.getObject
import ru.rainman.ui.helperutils.snack
import ru.rainman.ui.storage.StorageBottomSheet
import ru.rainman.ui.view.SpeakerChip

@AndroidEntryPoint
class PostEditorFragment : Fragment(R.layout.fragment_post_editor) {

    private lateinit var binding: FragmentPostEditorBinding
    private val viewModel: PostEditorViewModel by viewModels()
    private lateinit var navController: NavController
    private val args: PostEditorFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding = FragmentPostEditorBinding.bind(view)

        if (args.postId > 0) {
            viewModel.loadPost(args.postId)
        }

        navController =
            requireActivity().supportFragmentManager.getNavController(R.id.out_of_main_nav_host)

        viewModel.editablePost.observe(viewLifecycleOwner) {

            binding.inputContent.setText(it.content)

            val mentionedIsEmpty = it.mentioned.isEmpty()

            binding.mentionedUsersLayout.isVisible = !mentionedIsEmpty

            if (!mentionedIsEmpty) {
                binding.mentionedUsers.removeAllViews()
                it.mentioned.forEach { user ->
                    val chip = SpeakerChip(binding.mentionedUsers.context)
                    chip.setIconUrl(user.avatar)
                    chip.text = user.name
                    chip.isCloseIconVisible = true
                    chip.setOnCloseIconClickListener {
                        viewModel.removeSpeaker(user.id)
                    }
                    binding.mentionedUsers.addView(chip)
                }
            }

            binding.attachmentPreview.isVisible = it.attachment != null

            it.attachment?.let { att -> binding.attachmentPreview.setData(att) }
                ?: binding.attachmentPreview.recycle()


            it.coordinates.let { c ->

                binding.bottomBar.menu.findItem(R.id.myLocation).icon =
                    AppCompatResources.getDrawable(
                        requireContext(),
                        if (c == null) R.drawable.location_off
                        else R.drawable.location_on
                    )
                binding.bottomBar.invalidateMenu()
            }

        }

        binding.appBar.setNavigationOnClickListener {
            navController.popBackStack()
        }

        setResultListener(RequestKey.POST_REQUEST_KEY_MENTIONED) { bundle ->
            viewModel.setMentioned(bundle.getLongArray(ArgKey.USERS.name)?.toList() ?: emptyList())
        }

        setResultListener(RequestKey.POST_REQUEST_KEY_ATTACHMENT) { bundle ->
            viewModel.setAttachment(bundle.getObject(ArgKey.ATTACHMENT.name))
        }

        binding.bottomBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.attachment -> {
                    val bundle = Bundle()
                    bundle.putString(ArgKey.ATTACHMENT, PubType.POST.name)
                    val dialog = StorageBottomSheet()
                    dialog.arguments = bundle
                    dialog.show(parentFragmentManager, "STORAGE")
                    true
                }

                R.id.mention -> {
                    navController.navigate(
                        PostEditorFragmentDirections.actionPostEditorFragmentToSelectUsersDialogFragment(
                            viewModel.editablePost.value?.mentioned
                                ?.map { it.id }
                                ?.toLongArray()
                                ?: longArrayOf(),
                            PubType.POST
                        )
                    )
                    true
                }

                R.id.myLocation -> {
                    viewModel.setLocation(
                        viewModel.editablePost.value?.coordinates?.let {
                            Point(20.0, 29.0)
                        }
                    )
                    true
                }
                else -> false
            }

        }

        binding.send.setOnClickListener {
            viewModel.publish(binding.inputContent.text.toString(), requireContext())
        }

        viewModel.postStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Status.Error -> snack(it.message)
                Status.Loading -> snack("SENDING...")
                Status.Success -> navController.navigateUp()
                null -> {}
            }
        }
    }
}