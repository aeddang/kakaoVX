package com.skeleton.component.item

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.leanback.widget.ImageCardView
import com.lib.page.Page
abstract class ItemImageCardView : ImageCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    open fun onBind(data:Any?){}
    open fun onUnbind(){}
    open fun updateBackgroundColor(isSelected:Boolean){}
}