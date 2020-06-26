package com.dagger.module.app

import android.app.Application
import android.content.Context
import com.kakaovx.homet.tv.BuildConfig
import com.kakaovx.homet.tv.page.viewmodel.ActivityModel
import com.kakaovx.homet.tv.page.viewmodel.FragmentProvider
import com.kakaovx.homet.tv.store.PageRepository
import com.kakaovx.homet.tv.store.api.ApiPath
import com.kakaovx.homet.tv.store.api.HomeTInterceptor
import com.kakaovx.homet.tv.store.api.account.AccountApi
import com.kakaovx.homet.tv.store.api.account.AccountManager
import com.kakaovx.homet.tv.store.api.homet.HometApi
import com.kakaovx.homet.tv.store.api.homet.HometManager
import com.kakaovx.homet.tv.store.api.wecandeo.WecandeoApi
import com.kakaovx.homet.tv.store.api.wecandeo.WecandeoManager
import com.kakaovx.homet.tv.store.database.DataBaseManager
import com.kakaovx.homet.tv.store.preference.AccountPreference
import com.kakaovx.homet.tv.store.preference.SettingPreference
import com.lib.page.PageActivityPresenter
import com.lib.page.PagePresenter
import com.skeleton.module.network.NetworkFactory

import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton


@Module
class StoreModule {

    @Provides
    @Named("appContext")
    fun provideContext(app: Application): Context = app.applicationContext
    @Provides
    @Singleton
    fun provideAccountPreference(application: Application): AccountPreference = AccountPreference(application)

    @Provides
    @Singleton
    fun provideSettingPreference(application: Application): SettingPreference = SettingPreference(application)

    @Provides
    @Singleton
    fun provideNetworkFactory(app: Application): NetworkFactory = NetworkFactory(app)

    @Provides
    @Singleton
    fun providePagePresenter (): PagePresenter = PageActivityPresenter ()

    @Provides
    @Singleton
    fun provideFragmentProvider (): FragmentProvider =
        FragmentProvider()

    @Provides
    @Singleton
    fun provideActivityModel (): ActivityModel =
        ActivityModel()

    @Provides
    @Singleton
    fun provideDataBaseManager(@Named("appContext") ctx: Context,
                               settingPreference:SettingPreference
    ): DataBaseManager = DataBaseManager(ctx, settingPreference)

    @Provides
    @Singleton
    fun provideHomeTInterceptor(): HomeTInterceptor= HomeTInterceptor()

    @Provides
    @Singleton
    fun provideAccountManager(@Named("appContext") ctx: Context,
                              accountPreference:AccountPreference,
                              settingPreference:SettingPreference,
                              networkFactory: NetworkFactory,
                              interceptor:HomeTInterceptor
    ): AccountManager = AccountManager(ctx, accountPreference, settingPreference,
        networkFactory.getRetrofit(BuildConfig.APP_REST_ADDRESS, listOf( interceptor ) ).create(AccountApi::class.java),
        interceptor)

    @Provides
    @Singleton
    fun provideHometManager(@Named("appContext") ctx: Context,
                              settingPreference:SettingPreference,
                              networkFactory: NetworkFactory,
                              interceptor:HomeTInterceptor,
                              accountManager: AccountManager
    ): HometManager = HometManager(ctx, settingPreference,
        networkFactory.getRetrofit(BuildConfig.APP_REST_ADDRESS, listOf( interceptor )).create(HometApi::class.java),
        accountManager)

    @Provides
    @Singleton
    fun provideWecandeoManager(@Named("appContext") ctx: Context,
                            networkFactory: NetworkFactory
    ): WecandeoManager = WecandeoManager(ctx,
        networkFactory.getRetrofit(ApiPath.WECANDEO_ADDRESS).create(WecandeoApi::class.java))

    @Provides
    @Singleton
    fun providePageRepository(@Named("appContext") ctx: Context,
                              settingPreference:SettingPreference,
                              dataBaseManager: DataBaseManager,
                              accountManager: AccountManager,
                              hometManager: HometManager,
                              wecandeoManager: WecandeoManager,
                              pageModel: ActivityModel,
                              pageProvider: FragmentProvider,
                              pagePresenter: PagePresenter
    ): PageRepository = PageRepository(ctx, settingPreference, dataBaseManager,
        accountManager, hometManager, wecandeoManager, pageModel, pageProvider, pagePresenter)
}