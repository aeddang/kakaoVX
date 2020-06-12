package com.kakaovx.homet.tv.page.viewmodel

import com.kakaovx.homet.tv.page.error.PageError
import com.kakaovx.homet.tv.page.home.PageHome
import com.kakaovx.homet.tv.page.program.PageProgram
import com.lib.page.PageProvider
import com.lib.page.PageObject
import com.lib.page.PageViewFragment

class FragmentProvider : PageProvider{
    fun getPageObject(pageID: PageID) = PageObject(pageID.value, pageID.position)
    override fun getPageView(pageObject: PageObject): PageViewFragment {
        return when(pageObject.pageID){
            PageID.HOME.value -> PageHome()
            PageID.PROGRAM.value -> PageProgram()
            PageID.ERROR.value -> PageError()
            else -> PageHome()
        }
    }
}

enum class PageID(val value: String, val position: Int = 9999){
    HOME("home", 100),
    PROGRAM("program", 1000),
    ERROR("error", 9999),
}


