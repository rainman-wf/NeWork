package ru.rainman.ui.helperutils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import ru.rainman.common.log
import ru.rainman.domain.model.AppError
import ru.rainman.domain.model.LinkPreview
import ru.rainman.domain.model.UploadMedia
import ru.rainman.domain.model.geo.GeoObject
import ru.rainman.domain.model.geo.Point
import ru.rainman.ui.R
import java.io.Serializable
import ru.rainman.ui.fragments.VideoPlayerDialogFragment
import ru.rainman.ui.databinding.ViewLinkPreviewBinding
import ru.rainman.ui.databinding.ViewLocationPreviewBinding
import ru.rainman.ui.fragments.users.UsersDialogFragment
import ru.rainman.ui.helperutils.states.Error
import ru.rainman.ui.helperutils.states.InteractionResultState
import ru.rainman.ui.helperutils.states.Loading
import ru.rainman.ui.helperutils.states.Success
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


fun FragmentManager.getNavController(@IdRes host: Int) =
    (findFragmentById(host) as NavHostFragment).navController

fun Long.toStringDate(): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(this)
}

fun menuItemHandle(handler: () -> Any): Boolean {
    handler()
    return false
}

fun Uri.toFile(context: Context): File {
    val file = File.createTempFile("tmp_", null, context.cacheDir)
    file
        .outputStream()
        .use {
            val input = context.contentResolver.openInputStream(this)
            input?.copyTo(it)
            input?.close()
        }
    return file
}

fun Uri.toUploadMedia(context: Context): UploadMedia {

    val stream = context.contentResolver.openInputStream(this)
    val byteArray = stream?.readBytes()?.toList()

    stream?.close()

    val cursor =
        context.contentResolver.query(this, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
    val name = if (cursor != null && cursor.moveToFirst()) cursor.getString(0) else "file"

    cursor?.close()

    return UploadMedia(byteArray!!, name)
}

fun Fragment.snack(msg: String) {
    Snackbar.make(this.requireView(), msg, Snackbar.LENGTH_SHORT).show()
}

fun DialogFragment.snack(msg: String) {
    Snackbar.make(
        requireParentFragment().requireView(),
        msg,
        Snackbar.LENGTH_SHORT
    ).show()
}

fun Fragment.toast(msg: String) {
    Toast.makeText(this.requireContext(), msg, Toast.LENGTH_SHORT).show()
}

fun Fragment.activityFragmentManager(): FragmentManager {
    return this.requireActivity().supportFragmentManager
}


fun Int.asDuration(): String {
    val seconds = this / 1000
    val durationMinutes = seconds / 60
    val durationSeconds = DecimalFormat("00").format(seconds % 60)
    return "$durationMinutes:$durationSeconds"
}

fun Fragment.showVideoDialog(uri: String, ratio: Float) {
    val dialog = VideoPlayerDialogFragment()
    val bundle = Bundle()
    bundle.putString("uri", uri)
    bundle.putFloat("ratio", ratio)
    dialog.arguments = bundle
    dialog.show(parentFragmentManager, null)
}

fun Fragment.showUsersDialog(title: String, ids: List<Long>) {
    val dialog = UsersDialogFragment()
    val bundle = Bundle()
    bundle.putLongArray("ids", ids.toLongArray())
    bundle.putString("intention", title)
    dialog.arguments = bundle
    dialog.show(parentFragmentManager, null)
}

@Suppress("DEPRECATION")
inline fun <reified T : Serializable> Bundle.getObject(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(key, T::class.java)
    } else {
        getSerializable(key) as? T
    }
}

@ColorInt
fun Context.getColorAttribute(@AttrRes attr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

fun MenuItem.setIcon(context: Context, url: String?) {
    Glide.with(context)
        .asDrawable()
        .load(url)
        .circleCrop()
        .into(object : CustomTarget<Drawable>() {

            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable>?
            ) {
                this@setIcon.icon = resource
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                this@setIcon.icon = ContextCompat.getDrawable(context, R.drawable.avatar_error)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                this@setIcon.icon = ContextCompat.getDrawable(context, R.drawable.avatar_stub)
            }
        })
}

fun TextView.hyperLink(url: String, name: String? = null, onClickListener: () -> Unit) {
    val linkText = name ?: "://([^/]+)".toRegex().find(url)?.groupValues?.get(1)

    linkText?.let {
        val spannable = SpannableString(it)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                onClickListener()
            }
        }

        spannable.setSpan(
            clickableSpan,
            0,
            it.length,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        movementMethod = LinkMovementMethod.getInstance()
        setHintTextColor(Color.TRANSPARENT)
        text = spannable
    }
}

fun ViewLinkPreviewBinding.represent(link: LinkPreview) {

    root.visibility = View.VISIBLE

    link.apply {
        linkName.hyperLink(url, siteName) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            root.context.startActivity(intent)
        }

        optionalProps.isVisible = !isEmpty

        if (!isEmpty) linkTitle.text = title

        linkDescription.text = description
        image?.apply {
            Glide.with(root.context)
                .load(this)
                .placeholder(R.drawable.web)
                .error(R.drawable.web)
                .into(linkImg)
        }
    }
}

fun ViewLocationPreviewBinding.represent(geoObject: GeoObject) {

    root.visibility = View.VISIBLE

    val point = geoObject.geometry.firstOrNull() as? Point
        ?: throw NullPointerException("GeoObject point missed")

    val link =
        "https://static-maps.yandex.ru/1.x/?ll=${point.toRequestPairString()}&size=128,128&z=14&l=map&pt=${point.toRequestPairString()},pm2rdl1&apikey=c89d3902-fb56-41c9-9a1d-3a51c20ac3f8"

    log(link)

    geoObject.apply {
        locationName.text = name ?: "Undefined location"
        locationDescription.text = descriptionText ?: "Undefined description"
        Glide.with(root.context)
            .load(link)
            .into(mapPreview)
    }

}

private fun Point.toRequestPairString(): String {
    return "${this.latitude.toString().replace(",", ".")},${
        this.longitude.toString().replace(",", ".")
    }"
}

fun ViewModel.interact(
    catchTo: SingleLiveEvent<InteractionResultState>? = null,
    body: suspend () -> Unit
) {
    catchTo?.postValue(Loading)
    viewModelScope.launch {
        try {
            body()
            catchTo?.postValue(Success)
        } catch (e: AppError) {
            log(e.stackTraceToString())
            catchTo?.postValue(Error(e.message ?: "Unknown error"))?: return@launch
        }
    }
}