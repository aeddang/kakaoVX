package com.kakaovx.homet.tv.page.player.model
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData

enum class PlayerUIEvent(var value:Long = 0){
    Resume, Pause, Seek, UIHidden, UIView, UIUse, SeekMove, ListHidden, ListView,

}
enum class PlayerUiStatus(var value:Long = 0){
    Hidden, View, Use
}
enum class PlayerListStatus(var value:Long = 0){
    Playing, ListSearch
}

enum class PlayerStreamEvent(val value:Int = 0){
    Resumed, Paused, Seeked, Completed, Load, Loaded, Error
}

enum class PlayerStatus{
    Resume, Pause, Completed, Error, Stop
}

enum class PlayerStreamStatus(val value:Double = 0.0){
    Buffering, Playing, Stop
}

class Player {
    val uiEvent = MutableLiveData<PlayerUIEvent>()
    val streamEvent = MutableLiveData<PlayerStreamEvent>()
    val streamStatus = MutableLiveData<PlayerStreamStatus>()
    var playerStatus = PlayerStatus.Stop
    var playerUIStatus = PlayerUiStatus.View
    var playerListStatus = PlayerListStatus.Playing
    fun disposeLifecycleOwner(owner: LifecycleOwner){
        uiEvent.removeObservers(owner)
        streamEvent.removeObservers(owner)
        streamStatus.removeObservers(owner)
    }
}

