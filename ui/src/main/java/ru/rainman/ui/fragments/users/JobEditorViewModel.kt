package ru.rainman.ui.fragments.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.rainman.common.log
import ru.rainman.domain.dto.NewJobDto
import ru.rainman.domain.repository.JobRepository
import ru.rainman.domain.repository.UserRepository
import ru.rainman.ui.helperutils.EditableJob
import ru.rainman.ui.helperutils.SingleLiveEvent
import ru.rainman.ui.helperutils.interact
import ru.rainman.ui.helperutils.states.InteractionResultState
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class JobEditorViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val token = userRepository.authToken.asLiveData(viewModelScope.coroutineContext).value
        ?: throw IllegalArgumentException("login error")

    private val _job = MutableLiveData(EditableJob(0, token.id))
    val job: LiveData<EditableJob> get() = _job

    val oldName = SingleLiveEvent<String>()
    val oldPosition = SingleLiveEvent<String>()
    val oldLink = SingleLiveEvent<String>()

    val sendingState = SingleLiveEvent<InteractionResultState>()

    fun loadJob(jobId: Long) {
        viewModelScope.launch {
            val editable = jobRepository.getById(jobId)?.let {
                oldName.postValue(it.name)
                oldPosition.postValue(it.position)
                it.link?.let { link -> oldLink.postValue(link.url) }
                EditableJob(
                    id = it.id,
                    ownerId = token.id,
                    start = LocalDate.from(it.start),
                    finish = it.finish?.let { dateTime -> LocalDate.from(dateTime) },
                )
            }
            _job.postValue(editable)
        }
    }

    fun save(name: String, position: String, link: String? = null) {

        val dto = _job.value!!.let {

            NewJobDto(
                id = it.id,
                ownerId = it.ownerId,
                name = name,
                position = position,
                start = it.start,
                finish = it.finish,
                link = link,
            )
        }

        interact(sendingState) {
            jobRepository.create(dto)
        }
    }

    fun setStart(value: Long) {
        _job.postValue(
            _job.value!!
                .copy(
                    start = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(value),
                        TimeZone.getDefault().toZoneId()
                    ).toLocalDate()
                )
        )
    }

    fun setFinish(value: Long) {
        _job.postValue(
            _job.value!!
                .copy(
                    finish = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(value),
                        TimeZone.getDefault().toZoneId()
                    ).toLocalDate()
                )
        )
    }
}