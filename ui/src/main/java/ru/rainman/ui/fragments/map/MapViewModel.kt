package ru.rainman.ui.fragments.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ru.rainman.common.toModel
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.VisibleRegion
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.runtime.network.NetworkError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.rainman.domain.model.geo.GeoObject
import ru.rainman.domain.repository.MapRepository
import ru.rainman.ui.helperutils.SingleLiveEvent
import ru.rainman.ui.helperutils.interact
import ru.rainman.ui.helperutils.states.InteractionResultState
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapRepository: MapRepository
) : ViewModel() {

    private val _pointLiveData = MutableLiveData<GeoObject>()
    val pointLiveData: LiveData<GeoObject> = _pointLiveData

    val noConnection  = SingleLiveEvent<InteractionResultState>()

    private val _searchResulLiveData: MutableLiveData<List<GeoObject>> =
        mapRepository.geoObjects.asLiveData(viewModelScope.coroutineContext, 500)
                as MutableLiveData<List<GeoObject>>
    val searchResulLiveData: LiveData<List<GeoObject>> get() = _searchResulLiveData

    fun search(query: String, visibleRegion: VisibleRegion) {
        viewModelScope.launch {
            mapRepository.search(query, VisibleRegionUtils.toPolygon(visibleRegion).toModel())
        }
    }

    fun resetSearchResults() {
        interact(noConnection) {
            _searchResulLiveData.postValue(emptyList())
        }
    }

    fun getGeocode(point: Point) {
        interact(noConnection) {
            _pointLiveData.postValue(mapRepository.getGeocode(point.toModel()))
        }
    }
}