package com.dagger.module.app
import android.app.Application
import com.kakaovx.homet.tv.App


import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class AppModule {
    @Binds
    @Singleton
    internal abstract fun application(application: App): Application
}