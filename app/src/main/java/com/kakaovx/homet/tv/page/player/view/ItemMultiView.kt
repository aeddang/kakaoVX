package com.kakaovx.homet.tv.page.player.view

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.player.model.Movie
import com.kakaovx.homet.tv.store.api.homet.MovieUrlData
import com.lib.page.PageComponent
import com.skeleton.component.item.ItemImageCardView
import kotlinx.android.synthetic.main.cp_text.view.*


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

    var textView:Text? = null
    override fun onBind(data: Any?) {
        val movieUrlData = data as? MovieUrlData
        if(textView == null) {
            textView = Text(context)
            this.addView(textView)
        }

        movieUrlData?.let {m->
            textView!!.title.text = m.getIdxTitle(context)
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
        setBackgroundColor(context.resources.getColor(R.color.color_gray))
    }

    private fun passive(){
        setBackgroundColor(context.resources.getColor(R.color.color_gray_deep))
    }

    inner class Text : PageComponent{
        constructor(context: Context) : super(context)
        constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
        private val appTag = javaClass.simpleName


        @CallSuper
        override fun init(context: Context) {
            super.init(context)
        }


        override fun getLayoutResID(): Int = R.layout.cp_text


    }


}