package com.dagger.module.view

import com.dagger.PageScope
import com.kakaovx.homet.tv.store.PageRepository
import com.skeleton.module.ViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class FragmentModule {
    @Provides
    @PageScope
    fun provideViewModelFactory(repository: PageRepository): ViewModelFactory = ViewModelFactory(repository)
}