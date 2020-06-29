package com.kakaovx.homet.tv.page.popups

data class VideoData(var path:String){
    var title:String? = null
    var subtitle:String? = null
}

object Video{
    const val VIDEO = "video"
    const val PLAY_DATA = "playData"
}