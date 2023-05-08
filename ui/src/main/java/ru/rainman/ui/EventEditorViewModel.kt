package ru.rainman.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.rainman.ui.helperutils.TimeUnitsWrapper
import javax.inject.Inject

@HiltViewModel
class EventEditorViewModel @Inject constructor() : ViewModel(){

    private val _isOnline = MutableLiveData(true)
    val selected: LiveData<Boolean> get () = _isOnline

    private val _date = MutableLiveData<Long?>()
    val date: LiveData<Long?> get () = _date

    private val _time = MutableLiveData<TimeUnitsWrapper?>()
    val time: LiveData<TimeUnitsWrapper?> get () = _time

    fun online (value: Boolean) = _isOnline.postValue(value)
    fun setDate(value: Long?) = _date.postValue(value)
    fun setTime(value: TimeUnitsWrapper?) = _time.postValue(value)


}