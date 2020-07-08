package com.kakaovx.homet.tv.page.viewmodel

import android.content.pm.ActivityInfo
import androidx.lifecycle.MutableLiveData
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.component.tab.LeftTab
import com.lib.page.PageModel
import com.lib.page.PageObject

class ActivityModel : PageModel{
    override var isPageInit: Boolean = false
    override var currentPageObject: PageObject? = null
    override fun getHome(idx: Int): PageObject = PageObject(PageID.HOME.value, PageID.HOME.position)
    override fun getPageExitMessage(): Int  = R.string.notice_app_exit

    private val homePages = arrayOf(PageID.HOME, PageID.PROGRAM_LIST).map { it.value }
    override fun isHomePage(page: PageObject): Boolean {
        val f= homePages.indexOf(page.pageID)
        return f != -1
    }

    override fun getPageOrientation(page: PageObject): Int {
        return ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }

    private val disableHistoryPages = arrayOf(PageID.PLAYER).map { it.value }
    override fun isHistoryPage(page: PageObject): Boolean {
        val f= disableHistoryPages.indexOf(page.pageID)
        return f == -1
    }


    val backGroundImage = MutableLiveData<String>()


    private val tabViewPages = arrayOf(PageID.HOME,PageID.HOME_LIST, PageID.PROGRAM_LIST, PageID.SETTING, PageID.GUIDE).map { it.value }
    fun isTabView(id: String): Boolean {
        return tabViewPages.indexOf(id) != -1
    }

    var leftTab:LeftTab? = null

}