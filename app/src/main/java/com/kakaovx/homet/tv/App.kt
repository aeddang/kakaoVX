package com.kakaovx.homet.tv

import com.dagger.DaggerAppComponent
import com.facebook.stetho.Stetho
import com.kakaovx.homet.tv.util.AppUtil
import com.lib.util.Log
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject


class App : DaggerApplication(), HasAndroidInjector {

    private val appTag = javaClass.simpleName

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Any>

    override fun applicationInjector(): AndroidInjector<out App> {
        return DaggerAppComponent.builder().create(this)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return fragmentInjector
    }

    override fun onCreate() {
        super.onCreate()
        Log.enable = AppUtil.getDebugLevel()

        when (BuildConfig.BUILD_TYPE) {
            "debug" -> {
                Log.d(appTag, "Start Memory Debug")
                Log.d(appTag, "Start Remote Debug")
                Stetho.initializeWithDefaults(this)
            }
        }
    }

}