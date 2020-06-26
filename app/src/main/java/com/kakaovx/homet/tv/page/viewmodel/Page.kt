package com.kakaovx.homet.tv.page.viewmodel


enum class VideoError{
    HOST, PLAY_BACK
}

data class PageError<T>(val type:T, val code:String?, val msg:String? = null, val id: String? = null)