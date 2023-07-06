package ru.rainman.ui.fragments.publications.other

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.rainman.common.LinkPreviewBuilder
import ru.rainman.ui.helperutils.EditablePost
import ru.rainman.domain.dto.NewAttachmentDto
import ru.rainman.domain.dto.NewPostDto
import ru.rainman.domain.model.Attachment
import ru.rainman.domain.model.Coordinates
import ru.rainman.domain.model.LinkPreview
import ru.rainman.domain.model.Post
import ru.rainman.domain.model.RemoteMedia
import ru.rainman.domain.model.geo.Point
import ru.rainman.domain.repository.PostRepository
import ru.rainman.domain.repository.UserRepository
import ru.rainman.ui.helperutils.SingleLiveEvent
import ru.rainman.ui.helperutils.interact
import ru.rainman.ui.helperutils.states.InteractionResultState
import ru.rainman.ui.helperutils.toUploadMedia
import javax.inject.Inject

@HiltViewModel
class PostEditorViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val postStatus = SingleLiveEvent<InteractionResultState>()

    private val _linkPreview = MutableLiveData<LinkPreview?>()
    val linkPreview: LiveData<LinkPreview?> get() = _linkPreview

    val oldContent = SingleLiveEvent<String>()

    private val _editablePost = MutableLiveData(EditablePost())
    val editablePost: LiveData<EditablePost> get() = _editablePost

    private var isNewAttachment: Boolean = false


    fun loadPost(id: Long) {
        viewModelScope.launch {
            val post =
                postRepository.getById(id) ?: throw NullPointerException("post $id not found")
            _editablePost.postValue(post.toEditable())
        }
    }

    private fun Post.toEditable(): EditablePost {
        oldContent.postValue(content)
        return EditablePost(
            id = id,
            coordinates = coordinates,
            link = link?.url,
            attachment = attachment,
            mentioned = mentioned,
        )
    }


    fun removeSpeaker(userId: Long) {
        val old = editablePost.value!!.mentioned
        val new = old.filterNot { it.id == userId }
        val post = _editablePost.value!!.copy(mentioned = new)
        _editablePost.postValue(post)
    }

    fun setMentioned(ids: List<Long>) {
        viewModelScope.launch {
            val post = _editablePost.value!!.copy(mentioned = userRepository.getByIds(ids))
            _editablePost.postValue(post)
        }
    }

    fun setLocation(point: Point?) {
        _editablePost.postValue(_editablePost.value!!.copy(coordinates = point?.let {
            Coordinates(
                it.latitude,
                it.longitude
            )
        }))
    }


    fun publish(content: String, context: Context) {

        val dto = _editablePost.value!!.let {
            NewPostDto(
                id = it.id,
                content = content,
                coordinates = it.coordinates,
                link = _linkPreview.value?.url,
                attachment = it.attachment?.let { att ->
                    NewAttachmentDto(
                        type = when (att) {
                            is Attachment.Image -> NewAttachmentDto.Type.IMAGE
                            is Attachment.Video -> NewAttachmentDto.Type.VIDEO
                            is Attachment.Audio -> NewAttachmentDto.Type.AUDIO
                        },
                        media = if (isNewAttachment) att.uri.toUri()
                            .toUploadMedia(context) else RemoteMedia(att.uri)
                    )
                },
                mentionIds = it.mentioned.map { user -> user.id }
            )
        }

        interact(postStatus) { postRepository.create(dto) }
    }

    fun setAttachment(attachment: Attachment?) {
        val post = _editablePost.value!!.copy(attachment = attachment)
        _editablePost.postValue(post)
        isNewAttachment = true
    }

    fun loadLinkPreview(url: String?) {
        viewModelScope.launch {
            CoroutineScope(Dispatchers.IO).launch nested@{
                val link = try {
                    url?.let { LinkPreviewBuilder.poll(it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _linkPreview.postValue(null)
                    return@nested
                }
                _linkPreview.postValue(link)
            }
        }
    }
}