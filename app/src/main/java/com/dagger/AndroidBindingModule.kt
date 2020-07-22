package com.dagger


import com.dagger.module.view.ActivityModule
import com.dagger.module.view.PageActivityModule
import com.dagger.module.view.FragmentModule
import com.kakaovx.homet.tv.page.MainActivity
import com.kakaovx.homet.tv.page.popups.PageErrorSurport
import com.kakaovx.homet.tv.page.exercise.PageExercise
import com.kakaovx.homet.tv.page.exercise.PageExerciseList
import com.kakaovx.homet.tv.page.guide.PageGuide
import com.kakaovx.homet.tv.page.guide.PageGuideList
import com.kakaovx.homet.tv.page.home.PageHome
import com.kakaovx.homet.tv.page.home.PageHomeList
import com.kakaovx.homet.tv.page.player.PagePlayer
import com.kakaovx.homet.tv.page.popups.PageVideo
import com.kakaovx.homet.tv.page.popups.PageVideoExo
import com.kakaovx.homet.tv.page.popups.PageVideoView
import com.kakaovx.homet.tv.page.program.PageProgram
import com.kakaovx.homet.tv.page.program.PageProgramExerciseList
import com.kakaovx.homet.tv.page.program.PageProgramList
import com.kakaovx.homet.tv.page.setup.PageSetup
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class AndroidBindingModule {
    /**
     * Main Activity
     */
    @ActivityScope
    @ContributesAndroidInjector(modules = [PageActivityModule::class, ActivityModule::class])
    internal abstract fun bindMainActivity(): MainActivity

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageHome(): PageHome

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageHomeList(): PageHomeList

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageErrorSurport(): PageErrorSurport

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageProgram(): PageProgram

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageProgramExerciseList(): PageProgramExerciseList

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageProgramList(): PageProgramList

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageExercise(): PageExercise

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageExerciseList(): PageExerciseList


    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageVideo(): PageVideo

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageVideoView(): PageVideoView

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageVideoExo(): PageVideoExo

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPagePlayer(): PagePlayer

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageSetup(): PageSetup

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageGuide(): PageGuide

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageGuideList(): PageGuideList
}
