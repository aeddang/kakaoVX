package com.dagger


import com.dagger.module.view.ActivityModule
import com.dagger.module.view.PageActivityModule
import com.dagger.module.view.FragmentModule
import com.kakaovx.homet.tv.page.MainActivity
import com.kakaovx.homet.tv.page.popups.PageErrorSurport
import com.kakaovx.homet.tv.page.exercise.PageExercise
import com.kakaovx.homet.tv.page.home.PageHome
import com.kakaovx.homet.tv.page.popups.PageVideo
import com.kakaovx.homet.tv.page.popups.PageVideoExo
import com.kakaovx.homet.tv.page.popups.PageVideoView
import com.kakaovx.homet.tv.page.program.PageProgram
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
    internal abstract fun bindPageErrorSurport(): PageErrorSurport

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageProgram(): PageProgram

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageExercise(): PageExercise

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageVideo(): PageVideo

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageVideoView(): PageVideoView

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageVideoExo(): PageVideoExo

}
