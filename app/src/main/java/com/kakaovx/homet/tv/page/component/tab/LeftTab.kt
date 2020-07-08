package com.kakaovx.homet.tv.page.component.tab


import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.lib.util.animateX
import com.skeleton.component.tab.Tab
import kotlinx.android.synthetic.main.cp_left_tab.view.*
import kotlin.math.roundToInt


class LeftTab: Tab<PageID> {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    override fun getTabMenu(): Array<View> = arrayOf(tab0, tab1, tab2, tab3)
    override fun getIDData(): Array<PageID> = arrayOf(PageID.HOME, PageID.PROGRAM_LIST, PageID.GUIDE, PageID.SETTING )
    override fun getLayoutResID(): Int = R.layout.cp_left_tab

    fun getFocusTab(id:PageID):View?{
        if(!isView) return null
        val idx = getIDData().indexOf(id)
        if(idx == -1) return null
        return getTabMenu()[idx]
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        tab0.requestFocus()
    }

    private var isView = true
    fun viewTab() {
        if (isView) return
        isView = true
        isFocusable = true
        this.animateX(0, false).apply {
            interpolator = AccelerateInterpolator()
            startAnimation(this)
        }
    }

    fun hideTab() {
        if (! isView) return
        isView = false
        val pos = context.resources.getDimension(R.dimen.left_tab_width).roundToInt()
        isFocusable = false
        this.animateX(-pos, false).apply {
            interpolator = DecelerateInterpolator()
            startAnimation(this)
        }
    }

}