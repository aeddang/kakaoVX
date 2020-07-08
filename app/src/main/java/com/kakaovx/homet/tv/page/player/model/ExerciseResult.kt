package com.kakaovx.homet.tv.page.player.model
import io.reactivex.subjects.PublishSubject
import kotlin.collections.ArrayList
import kotlin.math.roundToLong


data class ExerciseResult (val exerciseType: String, val isExercise:Boolean = false){

    companion object {
        const val MIN_AVG:Double = 0.4
    }

    val appTag = javaClass.simpleName
    var title: String = "" ; internal set
    var movieId: String = "" ; internal set
    var motionId: String = "" ; internal set
    var motionMovieId: String = "" ; internal set
    var motionMovieRoundId: String = "" ; internal set
    var motionParts: String = "" ; internal set
    var rangeCount: Int = 0 ; internal set
    var startTime: Long = 0 ; internal set
    var endTime: Long = 0 ; internal set

    var duration: Long = 0 ; internal set
    var realRangeCount: Int = 0 ; internal set
    var exerciseTime: Long = 0 ; internal set

    var resultDuration: Long = 0 ; internal set
    var resultStartTime: Long = 0 ; internal set
    var resultEndTime: Long = 0 ; internal set
    var totalExerciseTime: Long = 0

    private val avgParts = arrayListOf<ArrayList<Float>>()
    private val setParts =  HashMap<String, Double>()

    var parts: Array<String>? = null
    set(value) {
        field = value
        avgParts.clear()
        field?.forEach {
            setParts[it.toString()] = 0.0
            avgParts.add(arrayListOf()) }
    }

    var allMotionAvg:Double = 0.0
    set(value) {
        if(value < 0.0) return
        field = if(value >= 1.0) 1.0 else value
    }


    private  var checkTime: Long = 0
    private  var finalTime: Long = 0
    private  var lastFiveTime: Long = -1L
    var checkAvg: Double = 0.0; private set
    var finalAvg: Double = 0.0; private set
    var progress: Long = 0


    val isEffectiveExercise:Boolean
        get() {
            return exerciseTime >= Exercise.MIN_EXERCISE_TIME
        }

    val isEffectiveExerciseHistory:Boolean
        get() {
            return if(isEffectiveExercise) true else  totalExerciseTime >= Exercise.MIN_EXERCISE_TIME
        }


    fun getProgressPct():Float{
        if(progress == 0L) return 0.0f
        return (progress - startTime).toFloat() / duration.toFloat()
    }

    fun getResultProgressPct():Float{
        if(progress == 0L) return 0.0f
        return (progress - resultStartTime).toFloat() / resultDuration.toFloat()
    }


    fun reset(){
        if(isEffectiveExercise) totalExerciseTime += exerciseTime
        progress = 0
        checkAvg = -1.0
        finalAvg = -1.0
        checkTime = startTime + (duration.toDouble() / 2.0).roundToLong()
        finalTime = startTime + duration - 2000
        lastFiveTime = startTime + duration - 6500
        realRangeCount = 0
        exerciseTime = 0
        bodyPoint = 0.0f
        shoulderPoint = 0.0f
        armPoint = 0.0f
        corePoint = 0.0f
        hipPoint = 0.0f
        legPoint = 0.0f
        shinPoint = 0.0f
        avgParts.forEach { it.clear() }
        setParts.keys.forEach { setParts[it] = 0.0 }
    }

    var bodyPoint: Float = 0.0f
    var shoulderPoint: Float = 0.0f
    var armPoint: Float = 0.0f
    var corePoint: Float = 0.0f
    var hipPoint: Float = 0.0f
    var legPoint: Float = 0.0f
    var shinPoint: Float = 0.0f
}