package com.kakaovx.homet.tv.page.player.model
import com.kakaovx.homet.tv.store.api.homet.ExerciseBreakTimeData
import com.kakaovx.homet.tv.store.api.homet.ExerciseMotionData
import com.kakaovx.homet.tv.store.api.homet.ExerciseMovieData
import com.kakaovx.homet.tv.util.secToLong
import com.lib.util.Log
import java.util.*
import kotlin.collections.ArrayList

object ExerciseParser{
    val appTag = javaClass.simpleName
    val MIN_STEP_TIME = 1000L

    private var prevFlag:Flag? = null
    private var progressTime:Long = 0L
    private var breakTimeOffset:Long = 0L
    private var flagIdx = 0
    private var currentMovie:Movie? = null
    private var currentMovieInfo:MovieInfo? = null
    private var movieParseTime = 0L
    private var movieProgressTime = 0L
    private var movieIdx = 0
    private var motionSetGroup = ""
    internal val flags:ArrayList<Flag> = ArrayList()
    enum class Type(val value:String){
        All("1"),Action("2"), Motion("3"), Intro("4"), Outro("5")
    }
    enum class PlayType(val value:String){
        Exercise("exercise"),Break("breakTime")
    }
    fun reset(){
        prevFlag = null
        progressTime = 0L
        flagIdx = 0
        currentMovie = null
        movieParseTime = 0L
        Log.d(appTag, "reset movieParseTime set $movieParseTime")
        movieProgressTime = 0L
        movieIdx = 0
        motionSetGroup = ""
        flags.clear()
    }
    fun addTypeAllOutro(){
        if(currentMovie != null && prevFlag != null && currentMovie!!.type.length > 5){
            val duration = currentMovie!!.duration
            val diff = duration - movieParseTime
            if(diff > 100L) {
                val f = addExplanationFlag(movieParseTime, duration )
                Log.d(appTag, "parseAll add Outro ${f.getInfo()}")
            }
        }
    }

    fun parse(data: ExerciseMovieData?, idx:Int):Movie?{
        data ?: return null
        data.motionMovieId ?: return null
        val type = data.motionMovieType ?: ""
        val typeID = if( type == Type.All.value) UUID.randomUUID().toString() else type
        var movie:Movie? = Movie(typeID, data.motionMovieId)
        movie?.init(data)
        motionSetGroup = if( data.movieGroup == null ||  data.movieGroup == "")  UUID.randomUUID().toString() else data.movieGroup
        Log.d(appTag, "motionSetGroup  $motionSetGroup ")
        if(currentMovie != movie ){
            addTypeAllOutro()
            currentMovie = movie
            currentMovie?.idx = movieIdx
            movieIdx ++
            movieParseTime = 0L
            breakTimeOffset = 0L
            movieProgressTime = progressTime
            Log.d(appTag, "currentMovie ${movie!!.duration}")
        }else{
            movie = null
        }
        val movieInfo = MovieInfo(idx)
        movieInfo.init(data)
        currentMovieInfo = movieInfo

        data.motions?.let{movieMotions->
            movieMotions.forEach { exercisePlayData ->
                if(type != Type.All.value) {
                    movieParseTime = 0L
                    breakTimeOffset = 0L
                    movieProgressTime = progressTime
                    Log.d(appTag, "motions foreach movieParseTime set $movieParseTime")
                }
                if(exercisePlayData.type == PlayType.Break.value){
                    parseBreak(exercisePlayData.breakTime)
                }else{
                    val cflag = when(type){
                        Type.All.value -> {
                            parseAll(exercisePlayData.motion)
                        }
                        else -> {
                            parseSet(exercisePlayData.motion, type)
                        }
                    }
                }
            }
        }

        return movie
    }

    private fun parseSet(data: ExerciseMotionData?, type:String):Flag?{
        data ?: return null
        val flag = when(type){
            Type.Action.value -> Flag(FlagType.Action, Type.Action)
            Type.Motion.value -> Flag(FlagType.Motion, Type.Motion)
            Type.Intro.value -> Flag(FlagType.Intro, Type.Intro)
            Type.Outro.value -> Flag(FlagType.Outro, Type.Outro)
            else -> Flag(FlagType.Explanation, Type.All)
        }
        flag.setData(data)
        flag.isMovieChange = true
        return addedFlag(flag)

    }

    private fun parseAll(data: ExerciseMotionData?):Flag?{
        data ?: return null
        val ctime = data.motionStartTime?.secToLong() ?: 0L
        if(movieParseTime > ctime) {
            addTypeAllOutro()
            Log.d(appTag, "parseAll motionTime :$ctime parseTime :$movieParseTime")
            movieParseTime = 0L
            Log.d(appTag, "parseAll movieParseTime set $movieParseTime")
        }

        val rctime = data.timerStart?.secToLong() ?: 0L
        val diff = ctime - movieParseTime
        Log.d(appTag, "ctime $ctime  movieParseTime $movieParseTime")
        if(diff > MIN_STEP_TIME) {
            val f = addExplanationFlag(movieParseTime, ctime)
            f.breakTimeOffset = breakTimeOffset
            Log.d(appTag, "parseAll add Explanation ${f.getInfo()}")
        }
        val hasMotion = ctime != rctime
        if (hasMotion){
            val mflag = Flag(FlagType.Motion, Type.All)
            mflag.setData(data)
            mflag.movieStartTime = ctime
            mflag.movieEndTime = rctime
            addedFlag(mflag)
            mflag.breakTimeOffset = breakTimeOffset
            Log.d(appTag, "parseAll add Motion ${mflag.getInfo()}")
        }
        val flag = Flag(FlagType.Action, Type.All)
        flag.setData(data)
        flag.breakTimeOffset = breakTimeOffset
        return addedFlag(flag)
    }

    private fun parseBreak(data: ExerciseBreakTimeData?){
        data ?: return
        val d = data.time?.secToLong() ?: 0
        if(d == 0L) return
        val flag = Flag(FlagType.Break, Type.All)
        flag.duration = d
        flag.movieStartTime = 0L
        flag.movieEndTime = d
        flag.isMovieChange = true
        breakTimeOffset += d
        addedFlag(flag)
        Log.d(appTag, "parseBreak ${flag.getInfo()}")
    }
    private fun addExplanationFlag(startTime:Long, endTime:Long):Flag{
        Log.d(appTag, "addExplanationFlag set $startTime")
        val type = if(startTime == 0L) FlagType.Intro else if(endTime == currentMovie?.duration) FlagType.Outro else FlagType.Explanation
        val flag = Flag(type, Type.All)
        flag.movieStartTime = startTime
        flag.movieEndTime = endTime
        flag.breakTimeOffset = breakTimeOffset
        return addedFlag(flag)
    }
    private fun addedFlag(flag:Flag):Flag{
        if(flag.type != FlagType.Break){
            currentMovie?.let {
                if(it.duration < flag.movieEndTime) flag.movieEndTime = it.duration
            }
        }
        val duration = flag.movieEndTime -  flag.movieStartTime
        if(duration <= 0){
            Log.e(appTag, "addedFlag duration error-> ${flag.getInfo()}")
            return flag
        }
        flag.duration = duration
        if(flag.type == FlagType.Action){
            flag.partLists = flag.motionParts.split(",").map { it }.toTypedArray()
            val result = ExerciseResult(flag.exerciseType)
            result.resultDuration = duration
            result.resultEndTime = flag.movieEndTime
            result.motionMovieRoundId = currentMovieInfo?.motionMovieRoundId ?: "0"
            result.title = flag.title
            result.motionParts = flag.motionParts
            result.movieId = currentMovie?.motionMovieUrls?.let{ it[0].movieId } ?: ""
            result.motionId = flag.id
            result.motionMovieId = currentMovieInfo?.id ?: ""
            result.duration = duration
            result.startTime = flag.movieStartTime
            result.endTime = flag.movieEndTime
            result.resultStartTime = flag.progressTime
            result.resultEndTime = flag.progressTime + flag.duration
            result.rangeCount = flag.count
            result.parts = flag.partLists
            flag.result = result
            prevFlag?.let {
                if(it.type == FlagType.Motion) {
                    flag.hasMotion = true
                    it.actionTitle = flag.title
                    flag.motionTitle = it.title
                    it.actionFlag = flag
                }
            }
        }
        prevFlag?.let {
            if(it.type == FlagType.Break) flag.isMovieChange = true
        }
        flag.progressTime = progressTime
        flag.movieProgressTime = if( flag.type==FlagType.Break ) progressTime
        else if(flag.parseType == Type.All) movieProgressTime
        else (movieProgressTime-flag.movieStartTime)
        flag.movieIndex = currentMovie?.idx ?: 0
        flag.idx = flagIdx
        flag.setId = motionSetGroup
        if(flag.movieStartTime == 0L) flag.isMovieChange = true

        if(flag.type != FlagType.Break){
            movieParseTime = flag.movieEndTime
            Log.d(appTag, "addedFlag movieParseTime set $movieParseTime")
        }

        flagIdx ++
        progressTime += flag.duration
        flag.progressEndTime = progressTime
        flags.add(flag)
        prevFlag = flag
        return flag
    }

}