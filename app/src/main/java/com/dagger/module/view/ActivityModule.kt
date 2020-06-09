package com.dagger.module.view

import android.app.Activity
import com.dagger.ActivityScope
import com.kakaovx.homet.tv.page.MainActivity
import com.kakaovx.homet.tv.store.PageRepository
import com.skeleton.module.ViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class PageActivityModule {
    @Binds
    @ActivityScope
    internal abstract fun activity(mainActivity: MainActivity): Activity
}

@Module
class ActivityModule {
    @Provides
    @ActivityScope
    fun provideViewModelFactory(repository: PageRepository): ViewModelFactory = ViewModelFactory(repository)
}
