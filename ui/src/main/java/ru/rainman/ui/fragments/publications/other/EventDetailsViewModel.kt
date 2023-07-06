package ru.rainman.ui.fragments.publications.other

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.rainman.common.toModel
import ru.rainman.domain.model.Event
import ru.rainman.domain.model.geo.GeoObject
import ru.rainman.domain.repository.EventRepository
import ru.rainman.domain.repository.MapRepository
import javax.inject.Inject


@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val mapRepository: MapRepository
): ViewModel() {

    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event> get() = _event

    private val _pointLiveData = MutableLiveData<GeoObject?>()
    val pointLiveData: LiveData<GeoObject?> = _pointLiveData

    fun getEvent(id: Long)  {
        viewModelScope.launch {
            _event.postValue(eventRepository.getById(id))
        }
    }

    fun getGeocode(point: Point) {
        viewModelScope.launch {
            _pointLiveData.postValue(mapRepository.getGeocode(point.toModel()))
        }
    }
}




