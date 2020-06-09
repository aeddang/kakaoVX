package com.kakaovx.homet.tv.store

import android.content.pm.ActivityInfo
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.store.PageID
import com.lib.page.PageModel
import com.lib.page.PageObject

class ActivityModel : PageModel{
    override var isPageInit: Boolean = false
    override var currentPageObject: PageObject? = null
    override fun getHome(idx: Int): PageObject = PageObject(PageID.HOME.value, PageID.HOME.position)
    override fun getPageExitMessage(): Int  = R.string.notice_app_exit
    override fun isHomePage(page: PageObject): Boolean {
        return when(page.pageID){
            PageID.HOME.value -> true
            else -> false
        }
    }

    override fun getPageOrientation(page: PageObject): Int {
        return ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }
}