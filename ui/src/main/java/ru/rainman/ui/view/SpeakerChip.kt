package ru.rainman.ui.view

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.chip.Chip
import ru.rainman.ui.R
import kotlin.properties.Delegates

class SpeakerChip(context: Context) : Chip(context, null, com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated) {

    var userId by Delegates.notNull<Long>()

    constructor(context: Context, userId: Long) : this(context) {
        this.userId = userId
//        chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
        elevation = 4f
    }

    fun setIconUrl(url: String?): SpeakerChip {
        Glide.with(context)
            .asDrawable()
            .override(24, 24)
            .circleCrop()
            .load(url)
            .placeholder(R.drawable.avatar_empty)
            .error(R.drawable.avatar_error)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    chipIcon = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    chipIcon = placeholder
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    chipIcon = errorDrawable
                }
            })
        return this
    }
}