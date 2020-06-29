package com.kakaovx.homet.tv.page.player.model

import android.content.Context
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.component.factory.StaticResource
import com.kakaovx.homet.tv.store.api.homet.ExerciseMotionData
import com.kakaovx.homet.tv.util.millisecToTimeString
import com.kakaovx.homet.tv.util.secToLong
import io.reactivex.subjects.PublishSubject


enum class FlagType(val value:String){
    Intro("intro"),
    Action("action"),
    Motion("motion"),
    Break("break"),
    Explanation("explanation"),
    Outro("outro")
}

data class FlagSet(val id:String) {
    var isInit = true; internal set
    var setNum = 0; internal set
}

data class Flag(val type:FlagType, val parseType:ExerciseParser.Type) {
    var id:String = "" ; internal set
    var idx:Int = 0; internal set
    var setId:String = ""; internal set
    var setIdx:Int = 0; internal set
    var motionIndex:Int = 0; internal set
    var motionCount:Int = 0; internal set

    var movieIndex:Int = -1; internal set
    var movieStartTime:Long = 0L; internal set
    var movieEndTime:Long = 0L; internal set
    var breakTimeOffset:Long = 0L; internal set
    var progressTime:Long = 0; internal set
    var progressEndTime:Long = 0; internal set
    var movieProgressTime:Long = 0; internal set
    var duration:Long = 0; internal set

    var title = "" ; internal set
    var motionTitle = "" ; internal set
    var actionTitle = "" ; internal set
    var speech: StaticResource? = null; internal set
    var thumbImg = ""; internal set
    var exerciseType = ""; internal set
    var intensivePart = "" ; internal set
    var motionParts = ""; internal set
    var motionDifficulty ="" ; internal set
    var checkPoint ="" ; internal set
    var count:Int = 0; internal set
    var rangeStart:Long = 0L; internal set
    var rangeEnd:Long = 0L; internal set
    var partLists:Array<String>? = null ; internal set
    var actionFlag:Flag? =  null
    var hasMotion = false
    var isActive = false
        set(value) {
            field = value
            activeObservable.onNext(field)
        }
    val isStep:Boolean
        get() {
            return type == FlagType.Motion || (type == FlagType.Action && !hasMotion)
        }

    fun getPrevSkip(skipMode:Boolean):Boolean{
        if(skipMode) return isSkip
        if( type == FlagType.Action && !hasMotion ) return true
        else return type != FlagType.Motion
    }
    fun getNextSkip(skipMode:Boolean):Boolean{
        if(skipMode) return isSkip
        if( type == FlagType.Action && !hasMotion ) return false
        else return type != FlagType.Motion
    }
    val isSkip:Boolean
        get() {
            return type != FlagType.Action
        }
    var isMovieChange:Boolean = false; internal set
    val activeObservable = PublishSubject.create<Boolean>()
    var isMute = false
    internal var result:ExerciseResult? = null

    fun getInfo():String{
        return "type:${type.value} title:$title movieIndex:$movieIndex  position:$movieStartTime ~ $movieEndTime  progress:$progressTime  duration:$duration isMovieProgress:$movieProgressTime"
    }

    fun getThumbImage():String{
        return if(thumbImg == "" && actionFlag != null) actionFlag!!.thumbImg
        else thumbImg
    }

    fun getFlagTitle():String{
        return if(title == "" ) motionTitle
        else title
    }

    fun getFlagMotionTitle():String{
        return if(motionTitle == "" ) title
        else motionTitle
    }

    fun getFlagActionTitle():String{
        return if(actionTitle == "" ) title
        else actionTitle
    }

    fun getFlagCount():Int{
        actionFlag ?: return count
        return actionFlag!!.count
    }

    fun getFlagResult():ExerciseResult?{
        actionFlag ?: return result
        return actionFlag?.result
    }

    fun getFlagDescription(context:Context):String{
        val viewCount = getFlagCount()
        val viewDuration = getFlagDuration()
        return if(viewCount == 0) viewDuration.millisecToTimeString()
        else "${viewDuration.millisecToTimeString()} | ${viewCount}${context.getString(R.string.unit_count)}"
    }

    fun getFlagDuration():Long{
        return if(type == FlagType.Action) duration else (duration + (actionFlag?.duration ?: 0L))
    }

    fun getFlagStep():Int{
        val step = motionCount
        return if( step == 0) 1 else step
    }

    internal fun setData(data: ExerciseMotionData){
        id = data.motionId ?: ""
        movieStartTime = data.timerStart?.secToLong() ?: 0L
        movieEndTime = data.timerEnd?.secToLong() ?: 0L
        title = data.title ?: ""
        intensivePart = data.intensivePart ?: ""
        thumbImg = data.imgUrl ?: ""
        if(type == FlagType.Action){
            count = data.rangeCount?.toInt() ?: 0
            exerciseType = data.exerciseType ?: ""
            motionParts = data.motionParts ?: ""
            rangeStart = data.rangeStart?.secToLong() ?: 0L
            rangeEnd = data.rangeEnd?.secToLong() ?: 0L
        }
    }
}
