package com.kakaovx.homet.tv.page.component.items

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.store.api.homet.MotionData
import com.skeleton.component.item.ItemImageCardView

class ItemMotion : ItemImageCardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)


    override fun onBind(data: Any?) {
        val motion = data as MotionData
        titleText = motion.title
        contentText = motion.motionDescription
        Glide.with(context)
            .load(motion.thumbnail)
            .centerCrop()
            .error( ContextCompat.getDrawable(context, R.drawable.movie) )
            .into(mainImageView)

    }

    override fun onUnbind() {
        badgeImage = null
        mainImage = null
    }

    override fun updateBackgroundColor(isSelected: Boolean) {
        //setBackgroundColor(color)
        //setInfoAreaBackgroundColor(color)
    }
}