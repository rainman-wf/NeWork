package ru.rainman.ui.storage.abstractions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class StorageViewModel<T : StorageItem> : ViewModel() {

    private val _data = MutableLiveData<List<T>>()
    val data: LiveData<List<T>> get() = _data

    fun loadData(itemList: List<T>) {
        _data.postValue(itemList)
    }
}