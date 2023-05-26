package ru.rainman.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.common_utils.toModel
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.VisibleRegion
import com.yandex.mapkit.map.VisibleRegionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.rainman.domain.model.geo.GeoObject
import ru.rainman.domain.repository.MapRepository
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapRepository: MapRepository
) : ViewModel() {

    private val _pointLiveData = MutableLiveData<GeoObject>()
    val pointLiveData: LiveData<GeoObject> = _pointLiveData

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
        _searchResulLiveData.postValue(emptyList())
    }

    fun getGeocode(point: Point) {
        viewModelScope.launch {
            _pointLiveData.postValue(mapRepository.getGeocode(point.toModel()))
        }
    }


}