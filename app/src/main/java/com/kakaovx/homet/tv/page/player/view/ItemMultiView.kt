package com.kakaovx.homet.tv.page.player.view

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.player.model.Movie
import com.kakaovx.homet.tv.store.api.homet.MovieUrlData
import com.skeleton.component.item.ItemImageCardView

class ItemMultiView : ItemImageCardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    init {
        cardType = CARD_TYPE_FLAG_IMAGE_ONLY
    }
    private val activeObserver = Observer<Int>{idx->
        if(idx == movieIdx) active()
        else passive()
    }

    private var movieIdx:Int = 0
    var movie:Movie? = null
    set(value) {
        value ?: return
        field = value
        value.lifecycleOwner?.let {
            value.multiViewIndex.observe(it, activeObserver)
        }

    }

    var textView:TextView? = null
    override fun onBind(data: Any?) {
        val movieUrlData = data as? MovieUrlData
        if(textView == null) {
            textView = TextView(context)
            this.addView(textView)
        }

        movieUrlData?.let {m->
            textView!!.text = m.idx.toString()
                Glide.with(context)
                .load(m.imgUrl)
                .centerCrop()
                .error( ContextCompat.getDrawable(context, R.drawable.ic_content_no_image) )
                .into(mainImageView)
            movieIdx = m.idx
            if(movie?.multiViewIndex?.value == movieIdx) active()
            else passive()
        }
    }

    override fun onUnbind() {
        badgeImage = null
        mainImage = null
        movie?.multiViewIndex?.removeObserver(activeObserver)
        movie = null
    }

    override fun updateBackgroundColor(isSelected: Boolean) {
        //setBackgroundColor(color)
        //setInfoAreaBackgroundColor(color)
    }

    private fun active(){
        setInfoAreaBackgroundColor(context.resources.getColor(R.color.color_gray_deep))
    }

    private fun passive(){
        setInfoAreaBackgroundColor(context.resources.getColor(R.color.color_gray))
    }


}