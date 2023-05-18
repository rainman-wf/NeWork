package ru.rainman.ui

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import ru.rainman.domain.model.User
import ru.rainman.domain.repository.ApiTestRepository
import ru.rainman.ui.helperutils.TimeUnitsWrapper
import java.io.File
import javax.inject.Inject
import kotlin.random.Random

val _usersS = (1..50).map {
    User(
        id = it.toLong(),
        name = "User #$it",
        login = "login_$it",
        favorite = Random.nextBoolean(),
        avatar = null
    )
}.toList()


@HiltViewModel
class EventEditorViewModel @Inject constructor(
    private val apiTestRepository: ApiTestRepository
) : ViewModel(){

    private val _isOnline = MutableLiveData(true)
    val selected: LiveData<Boolean> get () = _isOnline

    private val _date = MutableLiveData<Long?>()
    val date: LiveData<Long?> get () = _date

    private val _time = MutableLiveData<TimeUnitsWrapper?>()
    val time: LiveData<TimeUnitsWrapper?> get () = _time

    private val _speakers = MutableLiveData<List<User>>()
    val speakers: LiveData<List<User>> get() = _speakers

    private val _imgList = MutableLiveData<List<Uri>>(emptyList())
    val imgList: LiveData<List<Uri>> get() = _imgList

    fun online (value: Boolean) = _isOnline.postValue(value)
    fun setDate(value: Long?) = _date.postValue(value)
    fun setTime(value: TimeUnitsWrapper?) = _time.postValue(value)

    fun setSpeakers(ids: List<Long>) {
        _speakers.postValue(_usersS.filter { ids.contains(it.id) })
    }

    fun loadGallery(list: List<Uri>) {
        _imgList.postValue(list)
    }

    fun sendPhoto(file: File) {
        viewModelScope.launch {
            apiTestRepository.sendPhoto(file)
        }
    }

    fun sendVideo(file: File) {
        viewModelScope.launch {
            apiTestRepository.sendVideo(file)
        }
    }

    fun sendAudio(file: File) {
        viewModelScope.launch {
            apiTestRepository.sendAudio(file)
        }
    }

}