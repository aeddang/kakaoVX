package com.kakaovx.homet.tv.page.player.model

import com.kakaovx.homet.tv.store.api.homet.ExerciseMovieData
import com.kakaovx.homet.tv.store.api.homet.MovieUrlData
import io.reactivex.subjects.PublishSubject
import kotlin.math.roundToLong


data class Movie( val type:String, val id:String){
    var idx = 0; internal set
    var duration:Long = 0 ; private set
    var thumbImg = ""; private set
    var currentTime:Long = 0
        internal set(value){
            field = value
            currentTimeObservable.onNext(field)
        }
    val currentTimeObservable = PublishSubject.create<Long>()
    var screenPath = thumbImg; private set
        get() {
            motionMovieUrls ?: return thumbImg
            return motionMovieUrls!![multiViewIndex].movieUrl ?: ""
        }
    fun getScreenPath(idx:Int):String{
        return motionMovieUrls!![idx].movieUrl ?: ""
    }

    var mediaAccesskey = ""; private set
        get() {
            motionMovieUrls ?: return ""
            return motionMovieUrls!![multiViewIndex].mediaAccesskey ?: ""
        }
    fun getMediaAccesskey (idx:Int):String{
        return motionMovieUrls!![idx].mediaAccesskey ?: ""
    }

    var motionMovieUrls:ArrayList<MovieUrlData>? = null; private set
    var multiViewIndex = 4
        set(value) {
            field = value
            multiViewObservable.onNext(field)
        }
    val multiViewObservable = PublishSubject.create<Int>()


    var movieTitle = ""; private set
    fun init(data: ExerciseMovieData?){
        data ?: return
        data.motionMovieId ?: return
        movieTitle = data.motionMovieTitle ?: ""
        motionMovieUrls = data.motionMovieUrls
        motionMovieUrls?.let {
            thumbImg = it[0].imgUrl ?: ""
            if(it.size > 1) multiViewIndex = 4
            else multiViewIndex = 0
        }

        duration = data.playTime?.toDoubleOrNull()?.roundToLong() ?: 300000 // 5분
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





