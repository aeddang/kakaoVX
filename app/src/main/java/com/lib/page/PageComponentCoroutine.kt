package com.lib.page

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.CallSuper

abstract class PageComponentCoroutine : PageComponent, PageViewCoroutine{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    protected val scope = PageCoroutineScope()

    @CallSuper
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        scope.createJob()
        onCoroutineScope()
    }

    @CallSuper
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope.destoryJob()
    }
}