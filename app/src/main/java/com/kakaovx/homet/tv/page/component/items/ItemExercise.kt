package com.kakaovx.homet.tv.page.component.items

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.store.api.homet.ExerciseData
import com.skeleton.component.item.ItemImageCardView

class ItemExercise : ItemImageCardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)


    override fun onBind(data: Any?) {
        val exercise = data as ExerciseData
        titleText = exercise.title
        contentText = exercise.getSubTitle(context)
        Glide.with(context)
            .load(exercise.thumbnail)
            .centerCrop()
            .error( ContextCompat.getDrawable(context, R.drawable.ic_content_no_image) )
            .into(mainImageView)

    }

    override fun onUnbind() {
        badgeImage = null
        mainImage = null
    }

    override fun updateBackgroundColor(isSelected: Boolean) {
        setBackgroundColor(context.resources.getColor(R.color.color_gray_deep))
        setInfoAreaBackgroundColor(context.resources.getColor(R.color.color_gray_deep))
    }
}