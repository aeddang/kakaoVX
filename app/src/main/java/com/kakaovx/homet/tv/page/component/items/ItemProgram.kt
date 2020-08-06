package com.kakaovx.homet.tv.page.component.items

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.store.api.homet.ProgramData
import com.skeleton.component.item.ItemImageCardView

class ItemProgram : ItemImageCardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    init {

    }

    override fun onBind(data: Any?) {
        val program = data as ProgramData
        titleText = program.title
        contentText = program.getSubTitle(context)
        if( program.programId == null ){
            mainImageView.setImageResource(R.drawable.ic_recent)
        }else{
            Glide.with(context)
                .load(program.thumbnail)
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
        setBackgroundColor(context.resources.getColor(R.color.color_gray_deep))
        setInfoAreaBackgroundColor(context.resources.getColor(R.color.color_gray_deep))
    }
}