package com.kakaovx.homet.tv.page.player.model

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.kakaovx.homet.tv.store.api.homet.ExerciseMovieData
import com.kakaovx.homet.tv.store.api.homet.MovieUrlData
import com.kakaovx.homet.tv.util.secToLong
import io.reactivex.subjects.PublishSubject
import kotlin.math.roundToLong


data class Movie( val type:String, val id:String){
    var idx = 0; internal set
    var duration:Long = 0 ; private set
    var thumbImg = ""; private set


    var screenPath = thumbImg; private set
        get() {
            motionMovieUrls ?: return thumbImg
            return motionMovieUrls!![multiViewIndex.value ?: 0].movieUrl ?: ""
        }
    fun getScreenPath(idx:Int):String{
        return motionMovieUrls!![idx].movieUrl ?: ""
    }

    var mediaAccesskey = ""; private set
        get() {
            motionMovieUrls ?: return ""
            return motionMovieUrls!![multiViewIndex.value ?: 0].mediaAccesskey ?: ""
        }
    fun getMediaAccesskey (idx:Int):String{
        return motionMovieUrls!![idx].mediaAccesskey ?: ""
    }

    var lifecycleOwner:LifecycleOwner? = null
    val currentTime = MutableLiveData<Long>()
    var motionMovieUrls:ArrayList<MovieUrlData>? = null; private set
    val multiViewIndex = MutableLiveData<Int>()


    var movieTitle = ""; private set
    fun init(data: ExerciseMovieData?){
        data ?: return
        data.motionMovieId ?: return
        movieTitle = data.motionMovieTitle ?: ""
        motionMovieUrls = data.motionMovieUrls
        motionMovieUrls?.let {
            thumbImg = it[0].imgUrl ?: ""
            if(it.size > 1) multiViewIndex.value = 4
            else multiViewIndex.value = 0
        }
        duration = data.playTime?.secToLong() ?: 300000 // 5분
    }


    fun disposeLifecycleOwner(owner:LifecycleOwner){
        multiViewIndex.removeObservers(owner)
        currentTime.removeObservers(owner)
        lifecycleOwner?.let{
            multiViewIndex.removeObservers(owner)
            currentTime.removeObservers(owner)
        }
    }

}

data class MovieInfo(val idx:Int){
    var id = "" ; private set
    var motionMovieRoundId = "" ; private set
    var title = "" ; private set
    var difficulty = "" ; private set
    var exerciseTools = "" ; private set
    fun init(data: ExerciseMovieData?){
        data ?: return
        data.motionMovieId ?: return
        motionMovieRoundId = data.motionMovieRoundId ?: ""
        id = data.motionMovieId
        title = data.title ?: ""
        difficulty = data.difficulty ?: ""
        exerciseTools = data.exerciseTools ?: ""
    }

}





