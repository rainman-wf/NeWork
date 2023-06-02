package ru.rainman.ui.storage

import dagger.hilt.android.lifecycle.HiltViewModel
import ru.rainman.domain.model.Attachment
import ru.rainman.ui.storage.abstractions.StorageViewModel
import javax.inject.Inject

@HiltViewModel
class VideoStorageViewModel @Inject constructor(): StorageViewModel<Attachment.Video>()