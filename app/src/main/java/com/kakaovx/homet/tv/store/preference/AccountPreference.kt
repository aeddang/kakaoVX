package com.kakaovx.homet.tv.store.preference

import android.content.Context
import com.lib.module.CachedPreference

class AccountPreference(context: Context) : CachedPreference(context, PreferenceName.SETTING) {
    companion object {
        private const val JWT = "jwt"
    }

    fun setJWT(token: String) = put(JWT, token)
    fun getJWT(): String = get(JWT, "") as String
}