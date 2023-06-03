package ru.rainman.ui

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import ru.rainman.domain.dto.NewAttachmentDto
import ru.rainman.domain.dto.NewPostDto
import ru.rainman.domain.model.Attachment
import ru.rainman.domain.model.Coordinates
import ru.rainman.domain.model.UploadMedia
import ru.rainman.domain.model.User
import ru.rainman.domain.model.geo.Point
import ru.rainman.domain.repository.PostRepository
import ru.rainman.domain.repository.UserRepository
import ru.rainman.ui.helperutils.SingleLiveEvent
import ru.rainman.ui.helperutils.toUploadMedia
import javax.inject.Inject

@HiltViewModel
class PostEditorViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    enum class PublishingState {
        LOADING,
        ERROR,
        SUCCESS
    }

    private val _mentioned = MutableLiveData<List<User>>()
    val mentioned: LiveData<List<User>> get() = _mentioned

    val postStatus = SingleLiveEvent<PublishingState>()

    private val _attachment = MutableLiveData<Attachment?>()
    val attachment: LiveData<Attachment?> get() = _attachment


    private val _location = MutableLiveData<Point?>()
    val location: LiveData<Point?> get() = _location

    fun removeSpeaker(userId: Long) {
        val old = _mentioned.value?.toMutableList() ?: mutableListOf()
        val new = old.filterNot { it.id == userId }
        _mentioned.postValue(new)
    }

    fun setMentioned(ids: List<Long>) {
        viewModelScope.launch {
            _mentioned.postValue(userRepository.getByIds(ids))
        }
    }

    fun setLocation(point: Point?) {
        _location.postValue(point)
    }


    fun publish(content: String, context: Context) {

        val dto = NewPostDto(
            content = content,
            coordinates = _location.value?.let { Coordinates(it.latitude, it.longitude) },
            link = null,
            attachment = _attachment.value?.let {
                NewAttachmentDto(
                    type = when (it) {
                        is Attachment.Image -> NewAttachmentDto.Type.IMAGE
                        is Attachment.Video -> NewAttachmentDto.Type.VIDEO
                        is Attachment.Audio -> NewAttachmentDto.Type.AUDIO
                    },
                    media = it.uri.toUri().toUploadMedia(context)
                )
            },
            mentionIds = _mentioned.value?.map { it.id } ?: emptyList()
        )

        viewModelScope.launch {
            postStatus.postValue(PublishingState.LOADING)
            val event = postRepository.create(dto)
            if (event == null) {
                postStatus.postValue(PublishingState.ERROR)
            } else {
                postStatus.postValue(PublishingState.SUCCESS)
            }
        }
    }

    fun setAttachment(attachment: Attachment?) {
        _attachment.postValue(attachment)
    }
}