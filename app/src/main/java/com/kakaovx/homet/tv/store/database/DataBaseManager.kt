package com.kakaovx.homet.tv.store.database

import android.content.Context
import com.kakaovx.homet.tv.store.preference.SettingPreference

class DataBaseManager (ctx:Context, val settingPreference: SettingPreference){
    val contentDatabase = ContentDatabase(ctx)
}