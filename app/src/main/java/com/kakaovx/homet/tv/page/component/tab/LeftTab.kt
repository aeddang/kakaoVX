package com.kakaovx.homet.tv.page.component.tab


import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.viewmodel.PageID
import com.lib.page.Page
import com.lib.util.animateAlpha
import com.lib.util.animateFrame
import com.skeleton.component.tab.Tab
import kotlinx.android.synthetic.main.cp_left_tab.view.*
import kotlinx.android.synthetic.main.cp_left_tab_item.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt


class LeftTab: Tab<PageID> {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)

    override fun getIDData(): Array<PageID> = arrayOf(PageID.HOME, PageID.PROGRAM_LIST, PageID.GUIDE, PageID.SETUP )
    override fun getLayoutResID(): Int = R.layout.cp_left_tab

    fun getFocusTab(id:PageID):View?{
        if(!isView) return null
        val idx = getIDData().indexOf(id)
        if(idx == -1) return null
        return tab[idx]
    }

    override fun getTabMenu(): Array<View> {
        return getIDData().map {
            val tab = Item(context)
            body.addView(tab)
            tab.id = UUID.randomUUID().hashCode()
            tab.setType(it)
            tab
        }.toTypedArray()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    var isView = true; private set
    fun viewTab() {
        if (isView) return
        isView = true
        isFocusable = true
        val w = if(!isFocus) context.resources.getDimension(R.dimen.left_tab_width).roundToInt()
                        else context.resources.getDimension(R.dimen.left_tab_width_active).roundToInt()
        this.animateFrame(Rect(0,0,w, -1)).start()
    }

    fun hideTab() {
        if (! isView) return
        isView = false
        val w = context.resources.getDimension(R.dimen.left_tab_width).roundToInt()
        isFocusable = false
        this.animateFrame(Rect(-w,0,w, -1)).start()
    }

    private var isFocus = false
    private var updateJob:Job? = null
    private fun updateFocus(){
        if(!isView) return
        updateJob?.cancel()
        updateJob = scope.launch {
            delay(50)
            val focus = tab.find { it.isFocused }
            isFocus = focus != null
            isView = false
            viewTab()
             tab.map { it as Item }.forEach { if(isFocus) it.active() else it.passive() }
        }
    }


    inner class Item: LinearLayout, Page {
        constructor(context: Context): super(context) { init(context) }
        constructor(context: Context, attrs: AttributeSet): super(context, attrs) { init(context) }

        override fun getLayoutResID(): Int = R.layout.cp_left_tab_item

        fun init(context: Context) {
            LayoutInflater.from(context).inflate(getLayoutResID(), this, true)
        }

        private var imgRes = 0
        private var imgResOn = 0
        fun setType(pageID:PageID){

            var strRes = 0
            when(pageID){
                PageID.HOME ->{
                    imgRes = R.drawable.ic_home
                    imgResOn = R.drawable.ic_home_on
                    strRes = R.string.left_tab_home
                }
                PageID.PROGRAM_LIST ->{
                    imgRes = R.drawable.ic_program
                    imgResOn = R.drawable.ic_program_on
                    strRes = R.string.left_tab_program
                }
                PageID.GUIDE ->{
                    imgRes = R.drawable.ic_guide
                    imgResOn = R.drawable.ic_guide_on
                    strRes = R.string.left_tab_guide
                }
                PageID.SETUP ->{
                    imgRes = R.drawable.ic_setup
                    imgResOn = R.drawable.ic_setup_on
                    strRes = R.string.left_tab_setup
                }
            }
            icon.setImageResource(imgRes)
            title.setText(strRes)
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            title.visibility = View.GONE
            title.alpha = 0.0f

            this.setOnFocusChangeListener { _, hasFocus ->
                if(hasFocus){
                    setBackgroundResource(R.drawable.shape_rect_white_border3)
                    title.setTextAppearance(context, R.style.font_tab_select)
                    icon.setImageResource(imgResOn)
                }else{
                    setBackgroundResource(R.drawable.shape_transparent)
                    title.setTextAppearance(context, R.style.font_tab_default)
                    icon.setImageResource(imgRes)
                }
                updateFocus()
            }
        }

        fun active(){
            title.animateAlpha(1.0f)
        }

        fun passive(){
            title.animateAlpha(0.0f)
        }

    }

}