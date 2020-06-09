package com.kakaovx.homet.tv.store

import com.kakaovx.homet.tv.BuildConfig
import com.kakaovx.homet.tv.MainFragment
import com.kakaovx.homet.tv.VideoDetailsFragment
import com.kakaovx.homet.tv.page.error.PageError
import com.kakaovx.homet.tv.page.home.PageHome
import com.lib.page.PageProvider
import com.lib.page.PageObject
import com.lib.page.PageView
import com.lib.page.PageViewFragment

class FragmentProvider : PageProvider{
    fun getPageObject(pageID: PageID) = PageObject(pageID.value, pageID.position)
    override fun getPageView(pageObject: PageObject): PageViewFragment {
        return when(pageObject.pageID){
            PageID.HOME.value -> PageHome()
            PageID.ERROR.value -> PageError()
            else -> VideoDetailsFragment()
        }
    }
}

enum class PageID(val value: String, val position: Int = 9999){
    HOME("home", 100),
    SUB("sub", 1000),
    ERROR("error", 9999),
}


