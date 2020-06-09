package com.skeleton.module

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder


class ImageFactory{

    fun getBitmapLoader(context: Context): RequestBuilder<Bitmap>  = Glide.with(context).asBitmap()
    fun getItemDrawableLoader (context: Context): RequestBuilder<Drawable> = Glide.with(context).asDrawable()
    fun getBackgroundDrawableLoader(context: Context): RequestBuilder<Drawable> = Glide.with(context).asDrawable()
}