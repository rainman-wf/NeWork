package ru.rainman.ui.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.Drawable
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
import ru.rainman.domain.model.Attachment
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

    private var minRatio by Delegates.notNull<Float>()
    private var maxRatio by Delegates.notNull<Float>()
    private var defaultRatio by Delegates.notNull<Float>()

    private var attachment: Attachment? = null

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
            maxRatio = ta.getFloat(R.styleable.AttachmentView_max_ratio, 2f)
            minRatio = ta.getFloat(R.styleable.AttachmentView_min_ratio, 1f)
            defaultRatio = ta.getFloat(R.styleable.AttachmentView_default_ratio, 16f / 9f)
        }

        binding.image.updateLayoutParams<LayoutParams> {
            dimensionRatio = maxRatio.toString()
        }

        ta.recycle()
    }

    private fun setImage() {
        binding.playable.isVisible = false
        binding.image.isVisible = true
        binding.title.isVisible = false
        (attachment as Attachment.Image).let {
            binding.image.updateLayoutParams<LayoutParams> {
                dimensionRatio = it.ratio.toString()
            }
            Glide.with(binding.root.context)
                .load(it.uri)
                .placeholder(R.drawable.outline_ondemand_video_24)
                .error(R.drawable.outline_ondemand_video_24)
                .into(binding.image)
        }
        resetMediaMetadata()
    }

    private fun setAudio() {
        setPlayButtonPosition()
        binding.playable.isVisible = true
        binding.image.isVisible = false
        binding.title.isVisible = true

        (attachment as Attachment.Audio).let {
            binding.title.text = buildTitle(it.artist, it.title)
            binding.duration.text = it.duration.asDuration()
        }
    }


    private fun buildTitle(artist: String, title: String): SpannableString {
        val total = "$artist - $title"
        val spannableString = SpannableString(total)
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            artist.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    private fun setVideo() {
        setPlayButtonPosition()
        binding.playable.isVisible = true
        binding.image.isVisible = true
        binding.title.isVisible = false
        (attachment as Attachment.Video).let {
            binding.image.updateLayoutParams<LayoutParams> {
                dimensionRatio = it.ratio.toString()
            }
            Glide.with(binding.root.context)
                .load(it.uri)
                .placeholder(R.drawable.outline_ondemand_video_24)
                .error(R.drawable.outline_ondemand_video_24)
                .into(binding.image)
            binding.duration.text = it.duration.asDuration()
        }
    }

    private fun ImageView.setRatioFromBitmap(image: Bitmap) {
        val imgRatio = image.width.toFloat() / image.height
        val ratio = min(max(imgRatio, minRatio), maxRatio)
        (layoutParams as LayoutParams).dimensionRatio = "$ratio"
    }

    fun setData(attachment: Attachment) {
        binding.root.isVisible = true
        this.attachment = attachment
        setType()
    }

    private fun setType()  {
        return when (attachment) {
            is Attachment.Video -> setVideo()
            is Attachment.Audio -> setAudio()
            is Attachment.Image -> setImage()
            null -> recycle()
        }
    }

    private fun resetMediaMetadata() {
        binding.duration.text = null
        binding.title.text = null
    }

    fun recycle() {
        attachment = null
        binding.root.isVisible = false
        Glide.with(binding.root.context).clear(binding.image)
        resetMediaMetadata()
    }

    private fun setPlayButtonPosition() {
        when (attachment) {
            is Attachment.Audio -> {
                binding.play.updateLayoutParams<LayoutParams> {
                    endToEnd = LayoutParams.UNSET
                }
                binding.duration.updateLayoutParams<LayoutParams> {
                    topToTop = binding.play.id
                    bottomToBottom = binding.play.id
                }
            }

            is Attachment.Video -> {
                binding.play.updateLayoutParams<LayoutParams> {
                    endToEnd = LayoutParams.PARENT_ID
                }

                binding.duration.updateLayoutParams<LayoutParams> {
                    topToTop = LayoutParams.PARENT_ID
                    bottomToBottom = LayoutParams.UNSET
                }
            }

            else -> {}
        }
    }


    fun setOnPlayClickListener(body: () -> Unit) {
        binding.play.setOnClickListener {
            body()
        }
    }

    private fun ImageView.loadBitmap(url: String, @DrawableRes drawable: Int) {
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
                        dimensionRatio = defaultRatio.toString()
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    binding.image.setImageDrawable(errorDrawable)
                    binding.image.updateLayoutParams<LayoutParams> {
                        dimensionRatio = defaultRatio.toString()
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    fun setAudioPlayed(value: Boolean) {
        binding.play.isSelected = value
    }
}