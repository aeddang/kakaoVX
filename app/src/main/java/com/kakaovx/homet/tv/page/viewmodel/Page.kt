package com.kakaovx.homet.tv.page.viewmodel

data class PageError<T>(val type:T, val code:String?, val msg:String? = null, val id: String? = null)