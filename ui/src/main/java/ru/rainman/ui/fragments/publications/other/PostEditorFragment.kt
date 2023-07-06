package ru.rainman.ui.fragments.publications.other

import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ru.rainman.common.log
import ru.rainman.domain.model.geo.Point
import ru.rainman.ui.R
import ru.rainman.ui.databinding.FragmentPostEditorBinding
import ru.rainman.ui.helperutils.PubType
import ru.rainman.ui.helperutils.args.ArgKey
import ru.rainman.ui.helperutils.args.RequestKey
import ru.rainman.ui.helperutils.args.putString
import ru.rainman.ui.helperutils.args.setResultListener
import ru.rainman.ui.helperutils.getNavController
import ru.rainman.ui.helperutils.getObject
import ru.rainman.ui.helperutils.represent
import ru.rainman.ui.helperutils.snack
import ru.rainman.ui.helperutils.states.Error
import ru.rainman.ui.helperutils.states.Loading
import ru.rainman.ui.helperutils.states.Success
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

        viewModel.oldContent.observe(viewLifecycleOwner) {
            binding.inputContent.setText(it)
        }

        viewModel.editablePost.observe(viewLifecycleOwner) {

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
            binding.clearAttachment.isVisible = it.attachment != null

            binding.clearAttachment.setOnClickListener {
                viewModel.setAttachment(null)
            }

            it.attachment?.let { att -> binding.attachmentPreview.setData(att) }
                ?: binding.attachmentPreview.recycle()

        }

        binding.inputContent.addTextChangedListener {
            Linkify.addLinks(binding.inputContent, Linkify.WEB_URLS)
            val url = binding.inputContent.urls.firstOrNull()
            binding.inputContent.urls.map { it.url }.log()
            url?.let {
                viewModel.loadLinkPreview(it.url)
            } ?: viewModel.loadLinkPreview(null)
        }

        viewModel.linkPreview.observe(viewLifecycleOwner) {
            binding.link.root.isVisible = it != null
            it?.let { it1 -> binding.link.represent(it1) }
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
                is Error -> snack(it.message)
                Loading -> snack("SENDING...")
                Success -> navController.navigateUp()
                null -> {}
            }
        }
    }
}