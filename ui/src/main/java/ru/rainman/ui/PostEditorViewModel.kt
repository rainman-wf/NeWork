package ru.rainman.ui

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.rainman.ui.helperutils.EditablePost
import ru.rainman.domain.dto.NewAttachmentDto
import ru.rainman.domain.dto.NewPostDto
import ru.rainman.domain.model.Attachment
import ru.rainman.domain.model.Coordinates
import ru.rainman.domain.model.Post
import ru.rainman.domain.model.RemoteMedia
import ru.rainman.domain.model.geo.Point
import ru.rainman.domain.repository.PostRepository
import ru.rainman.domain.repository.UserRepository
import ru.rainman.ui.helperutils.SingleLiveEvent
import ru.rainman.ui.helperutils.Status
import ru.rainman.ui.helperutils.toUploadMedia
import javax.inject.Inject

@HiltViewModel
class PostEditorViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val postStatus = SingleLiveEvent<Status>()

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

    private fun Post.toEditable() = EditablePost(
        id = id,
        content = content,
        coordinates = coordinates,
        link = link?.url,
        attachment = attachment,
        mentioned = mentioned,
    )


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
                link = it.link,
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

        viewModelScope.launch {
            postStatus.postValue(Status.Loading)
            try {
                postRepository.create(dto)
                postStatus.postValue(Status.Success)
            } catch (e: Exception) {
                setError(e.message.toString())
            }
        }
    }

    fun setAttachment(attachment: Attachment?) {
        val post = _editablePost.value!!.copy(attachment = attachment)
        _editablePost.postValue(post)
        isNewAttachment = true
    }

    private fun setError(message: String) {
        postStatus.postValue(Status.Error(message))
    }
}