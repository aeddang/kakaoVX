package com.kakaovx.homet.tv.page.player.view

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.player.model.Flag
import com.skeleton.component.item.ItemImageCardView

class ItemFlag : ItemImageCardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val activeObserver = Observer<Boolean>{ac->
        if(ac) active()
        else passive( flag?.getFlagResult()?.isEffectiveExerciseHistory ?: false)
    }
    var flag:Flag? = null

    override fun onBind(data: Any?) {
        flag = data as? Flag
        flag?.let {f->
            titleText = f.getFlagTitle()
            contentText = f.getFlagDescription(context)
            Glide.with(context)
                .load(f.thumbImg)
                .centerCrop()
                .error( ContextCompat.getDrawable(context, R.drawable.movie) )
                .into(mainImageView)

            f.lifecycleOwner?.let {
                f.isActive.observe(it, activeObserver )
            }

            val result = f.getFlagResult()
            if(f.isActive.value == true) active()
            else passive(result?.isEffectiveExerciseHistory ?: false)
        }
    }

    override fun onUnbind() {
        badgeImage = null
        mainImage = null
        flag?.isActive?.removeObserver(activeObserver)
        flag = null
    }

    override fun updateBackgroundColor(isSelected: Boolean) {
        //setBackgroundColor(color)
        //setInfoAreaBackgroundColor(color)
    }

    private fun reStart(){
        setInfoAreaBackgroundColor(context.resources.getColor(R.color.color_gray))
    }

    private fun completed(){
        setInfoAreaBackgroundColor(context.resources.getColor(R.color.color_black))
    }

    private fun active(){
        setInfoAreaBackgroundColor(context.resources.getColor(R.color.color_gray_deep))
    }

    private fun passive(isEffectiveExercise:Boolean){
        setBackgroundColor( context.getColor(R.color.transparent) )
        if(isEffectiveExercise) completed()
        else reStart()
    }


}