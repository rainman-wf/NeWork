package ru.rainman.ui.storage

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.rainman.domain.model.Attachment
import ru.rainman.ui.helperutils.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class StorageViewModel @Inject constructor() : ViewModel() {

    val attachment = SingleLiveEvent<Attachment>()
}