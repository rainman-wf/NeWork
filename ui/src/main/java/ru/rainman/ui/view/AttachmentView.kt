package ru.rainman.ui.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import android.media.MediaMetadataRetriever.METADATA_KEY_TITLE
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import ru.rainman.domain.model.AttachmentType
import ru.rainman.domain.model.AttachmentType.*
import ru.rainman.ui.R
import ru.rainman.ui.databinding.ViewAttachmentBinding
import ru.rainman.ui.helperutils.asDuration
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

class AttachmentView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    var minRatio by Delegates.notNull<Float>()
    var maxRatio by Delegates.notNull<Float>()

    private var url: String? = null
    private var type: AttachmentType? = null

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val binding: ViewAttachmentBinding

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.view_attachment, this, true)
        binding = ViewAttachmentBinding.bind(this)

        initializeAttributes(attrs, defStyleAttr, defStyleRes)
    }

    private fun initializeAttributes(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        if (attrs == null) return

        val ta = context.obtainStyledAttributes(
            attrs,
            R.styleable.AttachmentView,
            defStyleAttr,
            defStyleRes
        )

        binding.apply {
            type = AttachmentType.values()[ta.getInt(R.styleable.AttachmentView_media_type, 0)]
            maxRatio = ta.getFloat(R.styleable.AttachmentView_max_ratio, 2f)
            minRatio = ta.getFloat(R.styleable.AttachmentView_min_ratio, 1f)
        }

        binding.image.updateLayoutParams<LayoutParams> {
            dimensionRatio = maxRatio.toString()
        }

        setType()
        ta.recycle()
    }

    private fun setImage() {
        binding.playable.isVisible = false
        binding.image.isVisible = true
        binding.title.isVisible = false
        binding.image.loadBitmap(R.drawable.image)
        resetMediaMetadata()
    }

    private fun setAudio() {
        setPlayButtonPosition()
        binding.playable.isVisible = true
        binding.image.isVisible = false
        binding.title.isVisible = true
        setMediaMetadata()
    }

    private fun setVideo() {
        setPlayButtonPosition()
        binding.playable.isVisible = true
        binding.image.isVisible = true
        binding.title.isVisible = false
        binding.image.loadBitmap(R.drawable.outline_ondemand_video_24)
        setMediaMetadata()
    }

    private fun ImageView.setRatioFromBitmap(image: Bitmap) {
        val imgRatio = image.width.toFloat() / image.height
        val ratio = min(max(imgRatio, minRatio), maxRatio)
        (layoutParams as LayoutParams).dimensionRatio = "$ratio"
    }

    fun setData(type: AttachmentType, url: String) {
        binding.root.isVisible = true
        this.type = type
        this.url = url
        setType()
    }

    private fun resetMediaMetadata() {
        binding.duration.text = null
        binding.title.text = null
    }

    private fun setMediaMetadata() {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(url)
            binding.duration.text = retriever.extractMetadata(METADATA_KEY_DURATION)?.toInt()?.asDuration()
            if (type == AUDIO)
                binding.title.text = buildTitle(retriever.extractMetadata(
                METADATA_KEY_ARTIST), retriever.extractMetadata(METADATA_KEY_TITLE))
        } catch (_: Exception) { }
    }

    private fun buildTitle(artist: String?, title: String?) : SpannableString {
        val _artist = artist ?: "Unknown artist"
        val _title = title ?: "unknown title"
        val total = "$_artist - $_title"
        val spannableString = SpannableString(total)
        spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, _artist.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }

    private fun setType() {
        when (type) {
            AUDIO -> setAudio()
            VIDEO -> setVideo()
            IMAGE -> setImage()
            null -> {}
        }
    }

    fun recycle() {
        url = null
        type = null
        binding.root.isVisible = false
        Glide.with(binding.root.context).clear(binding.image)
        resetMediaMetadata()
    }

    private fun setPlayButtonPosition() {
        when (type) {
            AUDIO -> {
                binding.play.updateLayoutParams<LayoutParams> {
                    endToEnd = LayoutParams.UNSET
                }
                binding.duration.updateLayoutParams<LayoutParams> {
                    topToTop = binding.play.id
                    bottomToBottom = binding.play.id
                }
            }

            VIDEO -> {
                binding.play.updateLayoutParams<LayoutParams> {
                    endToEnd = LayoutParams.PARENT_ID
                }

                binding.duration.updateLayoutParams<LayoutParams> {
                    topToTop = LayoutParams.PARENT_ID
                    bottomToBottom = LayoutParams.UNSET
                }
            }

            IMAGE, null -> {}
        }
    }

    private fun ImageView.loadBitmap(@DrawableRes drawable: Int) {
        Glide
            .with(binding.root.context)
            .asBitmap()
            .load(url)
            .placeholder(drawable)
            .error(drawable)
            .into(object : CustomTarget<Bitmap>() {

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    this@loadBitmap.setImageBitmap(resource)
                    this@loadBitmap.setRatioFromBitmap(resource)
                }

                override fun onLoadStarted(placeholder: Drawable?) {
                    binding.image.setImageDrawable(placeholder)
                    binding.image.updateLayoutParams<LayoutParams> {
                        dimensionRatio = "16:9"
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    binding.image.setImageDrawable(errorDrawable)
                    binding.image.updateLayoutParams<LayoutParams> {
                        dimensionRatio = "16:9"
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }
}