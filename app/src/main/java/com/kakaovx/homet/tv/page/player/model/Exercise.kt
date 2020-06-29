package com.kakaovx.homet.tv.page.player.model

import android.content.Context
import android.widget.Toast
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.page.component.factory.StaticResource
import com.kakaovx.homet.tv.store.api.homet.ExercisePlayData
import com.kakaovx.homet.tv.store.api.homet.ExerciseStartData
import com.kakaovx.homet.tv.util.percentToDouble
import com.kakaovx.homet.tv.util.secToLong
import com.kakaovx.homet.tv.util.toSafeInt
import com.lib.util.Log
import com.skeleton.component.alert.CustomToast

import io.reactivex.subjects.PublishSubject
import kotlin.math.abs

data class Exercise(val id:String, var context: Context?){
    companion object{
        const val MIN_EXERCISE_TIME:Long = 5000
    }

    val appTag = javaClass.simpleName
    val movies = ArrayList<Movie>()
    private val flags = ArrayList<Flag>()
    private val flagGroup =  ArrayList<ArrayList<Flag>>()
    private val flagSets = HashMap<String, FlagSet>()

    var duration:Long = 0; private set
    var playTime:Long = 0; private set
    var totalStep:Int = 0; private set
    private var relayPlayPoint = 0
    var isComplete = false ; private set
    lateinit var info:ExerciseInfo
    var result:ExerciseResult? = null; private set

    fun initSet(data: ExercisePlayData){
        exerciseTime = data.totalPlayExerciseTime?.secToLong() ?: 0
        relayPlayPoint  = data.relayPlayPoint?.toSafeInt() ?: 0
        info = ExerciseInfo(data.planId ?: "")
        info.isRelayPlay = relayPlayPoint != 0
        info.setMetadata(data)

        totalAvgCount = data.preExerciseCount?.toSafeInt() ?: 0
        val totalAvgSumPct = data.preExerciseAvgSum?.toDoubleOrNull() ?: 0.0
        totalAvgSum = totalAvgSumPct.percentToDouble()
        ExerciseParser.reset()
        data.exercises?.forEachIndexed { index, exerciseMovieData ->
            ExerciseParser.parse(exerciseMovieData, index)?.let { movies.add(it) }
        }
        ExerciseParser.addTypeAllOutro()
        flags.addAll(ExerciseParser.flags)
        ExerciseParser.reset()
        //flags.sortBy { it.progressTime }
        var mIdx = -1
        var mCount = 0
        var finalMotion:Flag? = null
        var group:ArrayList<Flag>? = null
        var flagSet : FlagSet? = null
        flags.forEachIndexed { index, flag ->
            flag.idx = index
            if( flag.isStep) {
                finalMotion?.let {prev->
                    prev.speech = StaticResource.NEXT
                    prev.actionFlag?.speech = StaticResource.NEXT
                }
                when(mCount){
                    0 -> {
                        flag.speech = StaticResource.FIRST
                        flag.actionFlag?.speech = StaticResource.FIRST
                    }
                    else -> finalMotion = flag
                }
                mCount++
            }

            if( flag.isStep || flag.type == FlagType.Break || flag.type == FlagType.Outro) {
                group = ArrayList()
                flagGroup.add(group!!)
                mIdx ++
            }
            if( flag.type == FlagType.Motion || flag.type == FlagType.Action ||  flag.type == FlagType.Break) group?.add(flag)

            flag.motionIndex = mIdx
            flag.motionCount = mCount
            if(flagSet == null) flagSet = FlagSet(flag.setId)
            flagSet?.let { fset->
                var set = fset
                if(fset.id != flag.setId){
                    flagSets[fset.id] = fset
                    set = FlagSet(flag.setId)
                    flagSet = set
                }

                if( flag.type == FlagType.Action ){
                    set.setNum ++
                    flag.setIdx = set.setNum
                }
            }
        }
        flagSet?.let { flagSets[it.id] = it }


        flags.last()?.let {
            duration = it.progressTime + it.duration
        }

        finalMotion?.let {prev-> prev.speech = StaticResource.LAST }
        totalStep = mCount


    }


    private var totalAvgSum :Double = 0.0
    private var totalAvgCount :Int = 0
    fun addAvg(avg:Double){
        totalAvgCount ++
        totalAvgSum += avg
    }
    fun getTotalAvg():Double{
        if(totalAvgCount == 0) return 0.0
        return totalAvgSum/totalAvgCount
    }


    fun setStartData(data:ExerciseStartData){
        info.playId = data.playId ?: ""
    }

    fun getProgressFrags():List<Flag>{
        return flags.filter { it.isStep }.map { it }
    }


    fun getListFrags():List<Flag>{
        return flags.filter {  it.isStep }.map { it }
    }



    var videoTime:Long = 0; private set
    var progressTime:Long = 0
        private set(value){
            field = value
            progressTimeObservable.onNext(field)
        }
    val progressTimeObservable = PublishSubject.create<Long>()

    var exerciseTime:Long = 0
        private set(value){
            field = value
            exerciseTimeObservable.onNext(field)
        }
    val exerciseTimeObservable = PublishSubject.create<Long>()

    var currentFlagGroup = -1
        private set(value) {
            if(field == value) return
            if(field != -1) flagGroup[field].forEach { it.isActive = false }
            field = value
            if(field != -1) flagGroup[field].forEach { it.isActive = true }
        }

    var isFlagSkipAble =true
    var flagIndex = -1
        private set(value) {
            if(!isFlagSkipAble) return
            if(field == value) return
            field = value
            Log.d(appTag, "current flagIndex change ${field}" )
            isFlagSkipAble = false
            currentFlag = flags[field]
        }

    var currentResult:ExerciseResult? = null
        private set(value) {
            if(field == value) return
            field = value
            field?.let {
                it.reset()
                currentResultObservable.onNext(it)
            }
        }
    val currentResultObservable = PublishSubject.create<ExerciseResult>()

    fun isInitSet(flag:Flag):Boolean{
        val set = flagSets[flag.setId]
        set ?: return false
        val isInit =   set.isInit ?: false
        set.isInit = false
        flagSets.keys.filter { it != flag.setId }.forEach { flagSets[it]?.isInit = true }
        //if(flag.parseType != ExerciseParser.Type.All) return true
        if( set.setNum == 0) return false
        return isInit
    }

    var currentFlag:Flag? = null
        private set(value) {
            if(field === value) return
            value?.let {
                if(it.type != FlagType.Break){
                    if (movieIndex != it.movieIndex || movieIndex == -1) {
                        movieIndex = it.movieIndex
                        nextFlag = null
                        Log.d(appTag, "currentFlag movie change ${it.movieIndex }" )
                        return
                    }
                }
            }
            field = value
            isFlagSkipAble = true
            field ?: return
            field?.let{flag->
                currentFlagGroup = flag.motionIndex
                //currentMovie?.motionIndex = flag.motionIndex
                val nextIdx = flagIndex + 1
                nextFlag = if( nextIdx >= flags.size) null
                else flags[nextIdx]
                if(flag.type == FlagType.Motion || flag.type == FlagType.Action){
                    val cresult = flag.result
                    if( cresult !== currentResult){
                        sendMotionResult()
                        currentResult = cresult
                        Log.d(appTag, "currentResult changed ${flag.getInfo()}" )
                    }
                }
                changeFlagObservable.onNext(flag)
                Log.d(appTag, "currentFlag changed ${flag.getInfo()}" )

                changedFlagObservable.onNext(flag)
            }
        }
    val resultObservable = PublishSubject.create<ExerciseResult>()
    val changeFlagObservable = PublishSubject.create<Flag>()
    val changedFlagObservable = PublishSubject.create<Flag>()
    var nextFlag:Flag? = null; private set
    fun synchronizedFlag(){
        val flag = flags[flagIndex]
        currentFlag = flag
        Log.d(appTag, "synchronizedFlag $flagIndex  $movieIndex" )
    }

    var movieIndex = -1
        private set(value) {
            if(field == value) return
            field = value
            currentMovie = movies[ movieIndex ]
        }
    var currentMovie:Movie? = null
        private set(value) {
            if(field === value) return
            field = value
            field?.let {
                it.currentTime = flags[flagIndex].movieStartTime
                Log.d(appTag, "currentMovie $movieIndex ${it.currentTime}")
                movieObservable.onNext(it)
            }
        }
    val movieObservable = PublishSubject.create<Movie>()

    var isSkipMode = false
        set(value) {
            if(field == value) return
            field = value
        }

    private fun modifyCompletedRelayPlayPoint(){
        if(flags.size-1 == relayPlayPoint){
            if( flags[relayPlayPoint].type == FlagType.Explanation ){
                var modifyComplete = false
                val findType = if(isSkipMode) FlagType.Action else FlagType.Motion
                do {
                    if(relayPlayPoint > 0){
                        relayPlayPoint--
                        if( flags[relayPlayPoint].type == findType ) modifyComplete = true
                    }else{
                        relayPlayPoint = 0
                        modifyComplete = true
                    }
                } while ( !modifyComplete )

            }
        }
    }

    fun start(isSkip:Boolean){
        isSkipMode = isSkip
        modifyCompletedRelayPlayPoint()
        flagIndex = if(info.isRelayPlay) relayPlayPoint else {
            totalAvgCount = 0
            totalAvgSum = 0.0
            if(isSkipMode) {
                val find = flags.find { it.type == FlagType.Action }
                find?.idx ?: 0
            }
            else 0
        }
        Log.d(appTag, "start $flagIndex" )
        relayPlayPoint = 0
        result = ExerciseResult(info.exerciseType, true)
        result?.endTime = duration
        result?.duration = duration

    }

    fun sendMotionResult(){
        currentResult?.let {
            resultObservable.onNext(it)
            it.reset()
            currentResult = null
        }
    }


    val completedObservable = PublishSubject.create<Exercise>()
    fun onComplete(isEnd: Boolean = false){
        if(isComplete) return
        if(isEnd) isComplete = true
        completedObservable.onNext(this)
    }


    fun prev(isSkip: Boolean? = null){
        val currentMidx = currentFlag?.motionIndex ?: -1
        if(flagIndex <= 0){
            context?.let { CustomToast.makeToast(it,R.string.page_player_first, Toast.LENGTH_SHORT ).show() }
            return
        }
        var idx = flagIndex-1

        if(isSkip == false || (!isSkipMode && isSkip == null)) {
            flagIndex = idx
        }else{
            do {
                var isSearch = true
                if(idx < 0){
                    isSearch = false
                    flagIndex = 0
                }else{
                    val flag = flags[idx]
                    val breakSkip = (isSkip == null && flag.type == FlagType.Break)
                    if( (!flag.getPrevSkip(isSkipMode) && flag.motionIndex != currentMidx) || breakSkip ){
                        isSearch = false
                        flagIndex = idx
                    }else idx --
                }
            } while ( isSearch )
        }

    }
    fun next(isSkip: Boolean? = null){
        val max = flags.size - 1
        if(flagIndex >= max){
            if(currentFlag?.type != FlagType.Action) onComplete(true)
            else context?.let { CustomToast.makeToast(it,R.string.page_player_last, Toast.LENGTH_SHORT ).show() }
            return
        }
        var idx = flagIndex+1
        if( isSkip == false || (!isSkipMode && isSkip == null)) {
            flagIndex = idx
        }else{
            do {
                var isSearch = true
                if(idx > max){
                    isSearch = false
                    if(isSkipMode  && isSkip == null ) onComplete(true)
                    else context?.let { CustomToast.makeToast(it,R.string.page_player_last, Toast.LENGTH_SHORT ).show() }
                }else{
                    val flag = flags[idx]
                    val breakSkip = (isSkip == null && flag.type == FlagType.Break)
                    if( !flag.getNextSkip(isSkipMode) || breakSkip ){
                        isSearch = false
                        flagIndex = idx
                    }else idx ++
                }
            } while ( isSearch )
        }
    }

    fun needSeek(flagTime:Long):Boolean{
        val need = abs(progressTime - flagTime) > 100L
        Log.d(appTag, "progressTime $progressTime flagTime $flagTime")
        Log.d(appTag, "needSeek $need")
        return need
    }

    fun changeFrag(index:Int){
        flagIndex = index
    }

    fun completeMovie(){
        if(currentFlag?.type == FlagType.Break) return
        Log.d(appTag, "completeMovie next")
        if(flagIndex == flags.size-1) onComplete(true)
        else next()
    }

    fun completeBreakTime(){
        if(currentFlag?.type != FlagType.Break) return
        Log.d(appTag, "completeBreakTime next")
        next()
    }

    fun changeVideoTime(changeTime:Long){
        currentFlag?.let{
            progressTime = it.movieProgressTime + it.breakTimeOffset + changeTime
            val delta = changeTime - videoTime
            changeExerciseTime( delta )
        }
        videoTime = changeTime
        if(currentFlag?.type == FlagType.Break) changeBreakTime(changeTime)
        else changeMovieTime(changeTime)
    }

    private fun changeBreakTime(changeTime:Long){}


    private fun changeMovieTime(changeTime:Long){
        currentMovie?.let {movie->
            movie.currentTime = changeTime
            //Log.d(appTag, "progress $progressTime  movie ${changeTime} ")
            currentFlag?.let { if(progressTime >= it.progressEndTime) {
                Log.d(appTag, "next progress $progressTime  ${it.type.value} ${it.progressEndTime} ${changeTime} ")
                //if(isSkipMode && currentFlag?.type == FlagType.Motion) return@let
                if(nextFlag != null) next() else onComplete(true)
            } }

        }
    }

    private fun changeExerciseTime( delta:Long){
        if(delta in 0..150) {
            exerciseTime += delta
            result?.let{
                it.progress = progressTime
                it.exerciseTime += delta
            }
            currentResult?.let{
                it.progress = videoTime
                it.exerciseTime += delta
            }
        }
    }



}