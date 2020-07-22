package com.dagger.module.app
import android.app.Application
import com.kakaovx.homet.tv.App
import com.kakaovx.homet.tv.page.component.factory.TTSFactory


import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class AppModule {
    @Binds
    @Singleton
    internal abstract fun application(application: App): Application
}

@Module
class FactoryModule{
    @Provides
    @Singleton
    fun provideTTSFactory(app: Application): TTSFactory = TTSFactory(app.applicationContext)
}