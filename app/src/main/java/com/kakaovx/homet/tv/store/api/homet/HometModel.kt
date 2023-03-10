package com.kakaovx.homet.tv.store.api.homet

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.util.millisecToTimeString
import com.kakaovx.homet.tv.util.secToMinTimeString
import com.kakaovx.homet.tv.util.secToTimeString

data class WakeupData(
    @SerializedName("wakeupIdx") val wakeupIdx: String?
)

data class GuideImageData(
    @SerializedName("images") val images: ArrayList<GuideImage>?
)

data class GuideImage(
    @SerializedName("imageId") val imageId: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("imgurl") val imgurl: String?
)

data class CategoryData(
    @SerializedName("codeId") var codeId: String?,
    @SerializedName("codeName") var codeName: String?
)

data class ProgramList(
    @SerializedName("totalCount") var totalCount: String?,
    @SerializedName("programs") var programs: ArrayList<ProgramData>?
)

data class ProgramData(
    @SerializedName("programId") var programId: String? = null,
    @SerializedName("thumbnail") var thumbnail: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("difficulty") var difficulty: String? = null,
    @SerializedName("difficultyName") var difficultyName: String? = null,
    @SerializedName("exerciseCount") var exerciseCount: String? = null,
    @SerializedName("isMultiView") var isMultiView: Boolean? = null,
    @SerializedName("viewStartTime") var viewStartTime: String? = null
){
    fun setNodata(ctx:Context):ProgramData{
        title = ctx.getString(R.string.list_recent_nodata)
        return this
    }

    fun getSubTitle(ctx:Context):String{
        if(programId == null) return ""
        return "$difficultyName · $exerciseCount${ctx.getString(R.string.unit_count)}"
    }

    var isLast = false
    var key = ""
}

data class ProgramDetailData(
    @SerializedName("title") val title: String?,
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("exerciseCount") val exerciseCount: String?,
    @SerializedName("difficulty") val difficulty: String?,
    @SerializedName("difficultyName") val difficultyName: String?,
    @SerializedName("purpose") val purpose: String?,
    @SerializedName("purpose2") val purpose2: String?,
    @SerializedName("purposeName") val purposeName: String?,
    @SerializedName("class") val programClass: String?,
    @SerializedName("className") val programClassName: String?,
    @SerializedName("exercisePlanCount") val exercisePlanCount: String?,
    @SerializedName("averagePlayTime") val averagePlayTime: String?,
    @SerializedName("isMultiView") val isMultiView: Boolean?,
    @SerializedName("movieType") val movieType: String?,
    @SerializedName("movieTypeName") val movieTypeName: String?,
    @SerializedName("description") val description: String?
){
    fun getSubTitle(ctx:Context?):String{
        ctx ?: return ""
        return "$difficultyName · $exerciseCount${ctx.getString(R.string.unit_count)}"
    }
}


data class ExerciseData(
    @SerializedName("exerciseId") val exerciseId: String?,
    @SerializedName("roundId") val roundId: String?,
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("playTime") val playTime: String?,
    @SerializedName("calorie") val calorie: String?,
    @SerializedName("movieType") val movieType: String?,
    @SerializedName("movieTypeName") val movieTypeName: String?,
    @SerializedName("bodyParts") val bodyParts: String?,
    @SerializedName("className") val programClassName: String?,
    @SerializedName("bodyPartsName") val bodyPartsName: String?,
    @SerializedName("isMultiView") val isMultiView: Boolean?
){
    fun getSubTitle(ctx:Context?):String{
        ctx ?: return ""
        val t = playTime ?: "0"
        return "$bodyPartsName · ${t.secToMinTimeString()}${ctx.getString(R.string.unit_min)} · $calorie${ctx.getString(R.string.unit_kcal)}"
    }
}

data class ExerciseDetailData(
    @SerializedName("exerciseId") val exerciseId: String?,
    @SerializedName("roundId") val roundId: String?,
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("bodyParts") val bodyParts: String?,
    @SerializedName("bodyPartsName") val bodyPartsName: String?,
    @SerializedName("playTime") val playTime: String?,
    @SerializedName("calorie") val calorie: String?,
    @SerializedName("exerciseTools") val exerciseTools: String?,
    @SerializedName("exerciseToolsName") val exerciseToolsName: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("isMultiView") val isMultiView: String?,
    @SerializedName("movieType") val movieType: String?,
    @SerializedName("movieTypeName") val movieTypeName: String?
){
    fun getTime(ctx:Context?):String{
        ctx ?: return ""
        playTime ?: return ""
        return "${playTime!!.secToMinTimeString()}${ctx.getString(R.string.unit_min)}"
    }
    fun getKal(ctx:Context?):String{
        ctx ?: return ""
        return "$calorie${ctx.getString(R.string.unit_kcal)}"
    }
}

data class ExerciseMotionsData(
    @SerializedName("totalCount") val totalCount: String?,
    @SerializedName("mediaAccessApiUrl") val mediaAccessApiUrl: String?,
    @SerializedName("mediaAccessApiKey") val mediaAccessApiKey: String?,
    @SerializedName("motions") val motions: List<MotionData>?
)

data class MotionData(
    @SerializedName("programId") var programId: String,
    @SerializedName("exerciseId") val exerciseId: String?,
    @SerializedName("motionMovieId") val motionMovieId: String?,
    @SerializedName("movieId") val movieId: String?,
    @SerializedName("motionId") val motionId: String?,
    @SerializedName("motionMovieRoundId") val motionMovieRoundId: String?,
    @SerializedName("motionSetGroup") val motionSetGroup: String?,
    @SerializedName("movieUrl") val movieUrl: String?,
    @SerializedName("movieARManifestUrl") val movieARManifestUrl: String?,
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("motionDescription") val motionDescription: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("playTime") val playTime: String?,
    @SerializedName("timerStart") val timerStart: String?,
    @SerializedName("timerEnd") val timerEnd: String?,
    @SerializedName("rangeStart") val rangeStart: String?,
    @SerializedName("rangeEnd") val rangeEnd: String?,
    @SerializedName("previewStart") val previewStart: String?,
    @SerializedName("previewEnd") val previewEnd: String?,
    @SerializedName("mediaAccesskey") val mediaAccesskey: String?,
    @SerializedName("count") val count: String?,
    @SerializedName("motionType") val motionType: String?,
    @SerializedName("isArType") val isArType: Boolean?
){
    fun getSubTitle(ctx:Context?):String{
        ctx ?: return ""
        val viewCount = count?.toInt() ?: 0
        val viewDuration = playTime?.secToTimeString() ?: "0"
        return if(viewCount == 0) viewDuration
        else "$viewDuration · ${viewCount}${ctx.getString(R.string.unit_count)}"
    }
}

data class ExercisePlayData(
    @SerializedName("planId") val planId: String?,
    @SerializedName("exerciseType") val exerciseType: String?,
    @SerializedName("totalPlayExerciseTime") val totalPlayExerciseTime: String?,
    @SerializedName("totalPlayTime") val totalPlayTime: String?,
    @SerializedName("preExerciseAvgSum") val preExerciseAvgSum: String?,
    @SerializedName("preExerciseCount") val preExerciseCount: String?,
    @SerializedName("isPreExerciseComplete") val isPreExerciseComplete: String?,
    @SerializedName("relayPlayPoint") val relayPlayPoint: String?,
    @SerializedName("mediaAccessApiUrl") val mediaAccessApiUrl: String?,
    @SerializedName("mediaAccessApiKey") val mediaAccessApiKey: String?,
    @SerializedName("exerciseTitle") val exerciseTitle: String?,
    @SerializedName("difficulty") val difficulty: String?,
    @SerializedName("difficultyName") val difficultyName: String?,
    @SerializedName("playTime") val playTime: String?,
    @SerializedName("calorie") val calorie: String?,
    @SerializedName("bodyParts") val bodyParts: String?,
    @SerializedName("bodyPartsName") val bodyPartsName: String?,
    @SerializedName("exercises") val exercises: ArrayList<ExerciseMovieData>? )

data class ExerciseMovieData(
    @SerializedName("motionMovieId") val motionMovieId: String?,
    @SerializedName("motionSetGroup") val motionSetGroup: String?,
    @SerializedName("movieGroup") val movieGroup: String?,
    @SerializedName("motionMovieRoundId") val motionMovieRoundId: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("difficulty") val difficulty: String?,
    @SerializedName("exerciseTools") val exerciseTools: String?,
    @SerializedName("playTime") val playTime: String?,
    @SerializedName("motionMovieUrls") val motionMovieUrls: ArrayList<MovieUrlData>?,
    @SerializedName("motionMovieType") val motionMovieType: String?,
    @SerializedName("motionMovieTitle") val  motionMovieTitle: String?,
    @SerializedName("motions") val motions: ArrayList<ExerciseFlagData>?
)

data class MovieUrlData(
    @SerializedName("movieId") val movieId: String?,
    @SerializedName("movieUrl") val movieUrl: String?,
    @SerializedName("imgUrl") val imgUrl: String?,
    @SerializedName("motionJsonUrl") val motionJsonUrl: String?,
    @SerializedName("mediaAccesskey") val mediaAccesskey: String?
){
    var idx:Int = 0
    fun getIdxTitle(ctx:Context?):String{
        ctx ?: return ""
        if(idx ==0){
            return ctx.getString(R.string.page_player_multiview_representation)
        }else{
            return (idx+1).toString()
        }
    }
}

data class ExerciseFlagData(
    @SerializedName("type") val type: String?,
    @SerializedName("breakTime") val breakTime: ExerciseBreakTimeData?,
    @SerializedName("motion") val motion: ExerciseMotionData?
)

data class ExerciseMotionData(
    @SerializedName("motionId") val motionId: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("imgUrl") val imgUrl: String?,
    @SerializedName("motionStartTime") val motionStartTime: String?,
    @SerializedName("timerStart") val timerStart: String?,
    @SerializedName("timerEnd") val timerEnd: String?,
    @SerializedName("intensivePart") val intensivePart: String?,
    @SerializedName("rangeStart") val rangeStart: String?,
    @SerializedName("rangeEnd") val rangeEnd: String?,
    @SerializedName("rangeCount") val rangeCount: String?,
    @SerializedName("exerciseType") val exerciseType: String?,
    @SerializedName("motionParts") val motionParts: String?,
    @SerializedName("motionDifficulty") val motionDifficulty: String?
)

data class ExerciseBreakTimeData(
    @SerializedName("time") val time: String?
)

data class ExerciseStartData(
    @SerializedName("playId") val playId: String? )

data class BreakTimeData(
    @SerializedName("title") val title: String?,
    @SerializedName("imgUrl") val imgUrl: String?)


data class ExerciseResultData(
    @SerializedName("totalExerciseTime") val totalExerciseTime: String?,
    @SerializedName("calorie") val calorie: String?,
    @SerializedName("averageAccuracy") val averageAccuracy: String?,
    @SerializedName("exercisePeople") val exercisePeople: String?,
    @SerializedName("progress") val progress: String?,
    @SerializedName("partsPeople") val partsPeople: String?,
    @SerializedName("myAverageAccuracy") val myAverageAccuracy: String?,
    @SerializedName("feedbackMsg") val feedbackMsg: String?,
    @SerializedName("motionFeedbackMsg") val motionFeedbackMsg: String?,
    @SerializedName("programTitle") val programTitle: String?,
    @SerializedName("planner") val planner: ArrayList<ExerciseResultPlan>?)

data class ExerciseResultPlan(
    @SerializedName("planId") val planId: String?,
    @SerializedName("programId") val programId: String?,
    @SerializedName("exerciseId") val exerciseId: String?,
    @SerializedName("roundId") val roundId: String?,
    @SerializedName("isComplete") val isComplete: String?,
    @SerializedName("completeIdx") val completeIdx: String?)
















