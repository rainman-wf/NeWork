package ru.rainman.ui.helperutils

import android.net.Uri
import java.io.Serializable

data class SimpleAttachment(
    val type: MediaType,
    val uri: Uri
) : Serializable
