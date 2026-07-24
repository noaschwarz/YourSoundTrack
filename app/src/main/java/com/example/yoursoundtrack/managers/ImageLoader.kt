package com.example.yoursoundtrack.managers

import android.widget.ImageView
import com.bumptech.glide.Glide

fun ImageView.loadAlbumCover(
    url: String?,
    placeholderRes: Int = android.R.drawable.ic_menu_gallery,
    errorRes: Int = android.R.drawable.ic_menu_report_image
) {
    if (url.isNullOrBlank()) {
        setImageResource(placeholderRes)
        return
    }

    Glide.with(context)
        .load(url)
        .placeholder(placeholderRes)
        .error(errorRes)
        .into(this)
}