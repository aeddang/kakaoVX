package com.lib.page
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.lifecycle.LifecycleOwner

abstract class PageComponent : FrameLayout, Page, PageView{
    constructor(context: Context): super(context) { init(context) }
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) { init(context) }
    protected open fun init(context: Context) {
        val resId = getLayoutResID()
        if(resId != -1) LayoutInflater.from(context).inflate(resId, this, true)
    }
    override var lifecycleOwner: LifecycleOwner? = null
        set(value) {
            field = value
            pageChileren?.forEach { it.lifecycleOwner = value }
            value ?: return
            onLifecycleOwner(value)
        }
    override val hasBackPressAction: Boolean
        get(){
            val f = pageChileren?.find { it.hasBackPressAction }
            f ?: return false
            return true
        }

    open fun onLifecycleOwner(owner: LifecycleOwner){}

    @CallSuper
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    @CallSuper
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lifecycleOwner = null
    }

    @CallSuper
    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        pageChileren?.forEach { it.onTransactionCompleted() }
    }

    @CallSuper
    override fun onCategoryChanged(pageObject: PageObject?) {
        super.onCategoryChanged(pageObject)
        pageChileren?.forEach { it.onCategoryChanged(pageObject) }
    }

    @CallSuper
    override fun onPageAdded(pageObject: PageObject) {
        super.onPageAdded(pageObject)
        pageChileren?.forEach { it.onPageAdded(pageObject) }
    }

    @CallSuper
    override fun onPageViewModel(vm: PageViewModel) {
        super.onPageViewModel(vm)
        pageChileren?.forEach { it.onPageViewModel(vm) }
    }

    @CallSuper
    override fun onPageParams(params: Map<String, Any?>) {
        super.onPageParams(params)
        pageChileren?.forEach { it.onPageParams(params) }
    }

    @CallSuper
    override fun onPageEvent(pageObject: PageObject?, type: String, data: Any?) {
        super.onPageEvent(pageObject, type, data)
        pageChileren?.forEach { it.onPageEvent(pageObject, type, data) }
    }

    @CallSuper
    override fun onPageReload() {
        super.onPageReload()
        pageChileren?.forEach { it.onPageReload() }
    }

    @CallSuper
    override fun onPageRemoved(pageObject: PageObject) {
        super.onPageRemoved(pageObject)
        pageChileren?.forEach { it.onPageRemoved(pageObject) }
    }

    @CallSuper
    override fun onPagePause() {
        pageChileren?.forEach { it.onPagePause() }
    }

    @CallSuper
    override fun onPageResume() {
        pageChileren?.forEach { it.onPageResume() }
    }
}