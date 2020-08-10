package com.kakaovx.homet.tv.page.viewmodel

import com.kakaovx.homet.tv.page.PageIntro
import com.kakaovx.homet.tv.page.popups.PageErrorSurport
import com.kakaovx.homet.tv.page.exercise.PageExercise
import com.kakaovx.homet.tv.page.exercise.PageExerciseList
import com.kakaovx.homet.tv.page.guide.PageGuide
import com.kakaovx.homet.tv.page.guide.PageGuideList
import com.kakaovx.homet.tv.page.home.PageHome
import com.kakaovx.homet.tv.page.home.PageHomeList
import com.kakaovx.homet.tv.page.player.PagePlayer
import com.kakaovx.homet.tv.page.player.PagePlayerList
import com.kakaovx.homet.tv.page.popups.PageVideo
import com.kakaovx.homet.tv.page.popups.PageVideoExo
import com.kakaovx.homet.tv.page.popups.PageVideoView
import com.kakaovx.homet.tv.page.program.PageProgram
import com.kakaovx.homet.tv.page.program.PageProgramExerciseList
import com.kakaovx.homet.tv.page.program.PageProgramList
import com.kakaovx.homet.tv.page.setup.PageSetup
import com.lib.page.PageProvider
import com.lib.page.PageObject
import com.lib.page.PageViewFragment

class FragmentProvider : PageProvider{
    fun getPageObject(pageID: PageID) = PageObject(pageID.value, pageID.position)
    override fun getPageView(pageObject: PageObject): PageViewFragment {
        return when(pageObject.pageID){
            PageID.INTRO.value -> PageIntro()
            PageID.HOME.value -> PageHome()
            PageID.HOME_LIST.value -> PageHomeList()
            PageID.PROGRAM_LIST.value -> PageProgramList()
            PageID.PROGRAM.value -> PageProgram()
            PageID.PROGRAM_EXERCISE_LIST.value -> PageProgramExerciseList()
            PageID.EXERCISE.value -> PageExercise()
            PageID.EXERCISE_LIST.value -> PageExerciseList()
            PageID.PLAYER.value -> PagePlayer()
            PageID.PLAYER_LIST.value -> PagePlayerList()
            PageID.SETUP.value -> PageSetup()
            PageID.GUIDE.value -> PageGuide()
            PageID.GUIDE_LIST.value -> PageGuideList()

            PageID.ERROR_SURPORT.value -> PageErrorSurport()
            PageID.VIDEO.value -> PageVideo()
            PageID.VIDEO_VIEW.value -> PageVideoView()
            PageID.VIDEO_EXO.value -> PageVideoExo()
            else -> PageHome()
        }
    }
}

enum class PageID(val value: String, val position: Int = 9999){
    INTRO("intro", 1),
    HOME("home", 100),
    HOME_LIST("homeList", 101),
    PROGRAM_LIST("programList", 200),
    GUIDE("guide", 300),
    GUIDE_LIST("guide_list", 301),
    SETUP("setup", 400),
    PROGRAM("program", 1000),
    PROGRAM_EXERCISE_LIST("programExerciseList", 1001),
    EXERCISE("exercise", 2000),
    EXERCISE_LIST("exerciseList", 2001),
    PLAYER("player", 4000),
    PLAYER_LIST("player_List", 4001),
    ERROR_SURPORT("error", 9999),
    VIDEO("video", 9999),
    VIDEO_VIEW("videoView", 9999),
    VIDEO_EXO("videoExo", 9999)
}


