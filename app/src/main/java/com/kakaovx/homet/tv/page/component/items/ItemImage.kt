package com.kakaovx.homet.tv.page.component.items

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.store.api.homet.ExerciseData
import com.skeleton.component.item.ItemImageCardView

class ItemImage : ItemImageCardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    init {
        cardType = CARD_TYPE_FLAG_IMAGE_ONLY
    }
    var index:Int = -1; private set
    override fun onBind(data: Any?) {
        val set = data as Pair<String, Int>?
        set?.let {
            this.index = it.second
            Glide.with(context)
                .load(it.first)
                .centerCrop()
                .error( ContextCompat.getDrawable(context, R.drawable.ic_content_no_image) )
                .into(mainImageView)
        }

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