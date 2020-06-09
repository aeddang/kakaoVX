package com.kakaovx.homet.tv.page.home.component

import android.content.Context
import android.util.AttributeSet
import com.lib.page.PageComponentCoroutine
import com.lib.page.PageViewModel

class HomeBox : PageComponentCoroutine{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)

    override fun getLayoutResID(): Int {
        TODO("Not yet implemented")
    }

    override fun onPageParams(params: Map<String, Any?>) {
        super.onPageParams(params)
    }

    override fun onPageViewModel(vm: PageViewModel) {
        super.onPageViewModel(vm)


    }


}