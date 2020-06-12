package com.dagger


import com.dagger.module.view.ActivityModule
import com.dagger.module.view.PageActivityModule
import com.dagger.module.view.FragmentModule
import com.kakaovx.homet.tv.page.MainActivity
import com.kakaovx.homet.tv.page.error.PageError
import com.kakaovx.homet.tv.page.home.PageHome
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
    internal abstract fun bindPageError(): PageError

    @PageScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindPageProgram(): PageProgram



}
