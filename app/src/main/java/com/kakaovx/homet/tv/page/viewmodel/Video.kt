package com.kakaovx.homet.tv.page.viewmodel

data class VideoData(var path:String){
    var title:String? = null
    var subtitle:String? = null
    var startTime:Long = 0
    var endTime:Long = 0
}

object Video{
    const val VIDEO = "video"
    const val PLAY_DATA = "playData"
    const val PLAY_DATAS = "playDatas"
    const val PLAY_DATA_INDEX = "playDataIndex"
}

enum class VideoError{
    HOST, PLAY_BACK
}