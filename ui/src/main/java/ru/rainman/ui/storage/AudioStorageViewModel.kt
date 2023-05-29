package ru.rainman.ui.storage

import dagger.hilt.android.lifecycle.HiltViewModel
import ru.rainman.ui.storage.abstractions.StorageItem
import ru.rainman.ui.storage.abstractions.StorageViewModel
import javax.inject.Inject

@HiltViewModel
class AudioStorageViewModel @Inject constructor(): StorageViewModel<StorageItem.Audio>()