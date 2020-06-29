package com.kakaovx.homet.tv.page.player.model

import android.content.Context
import com.kakaovx.homet.tv.R
import com.kakaovx.homet.tv.store.api.homet.ExercisePlayData
import com.kakaovx.homet.tv.util.secToMinTimeString
import com.kakaovx.homet.tv.util.toSafeBoolean


data class ExerciseInfo (val planId:String){
    var isRelayPlay = false; internal set
    var isPreExerciseComplete = false; private set
    var playId: String = ""; internal set
    var exerciseType: String = ""; private set
    var mediaAccessApiUrl:String? = null; private set
    var mediaAccessApiKey:String? = null; private set
    var exerciseTitle:String = ""; private set
    var difficulty: String = ""; private set
    var difficultyName: String = ""; private set
    var playTime: String = ""; private set
    var calorie: String = ""; private set
    var bodyParts: String = ""; private set
    var bodyPartsName: String = ""; private set

    internal fun setMetadata(data: ExercisePlayData){
        exerciseType = data.exerciseType ?: ""
        exerciseTitle = data.exerciseTitle ?: ""
        isPreExerciseComplete  = data.isPreExerciseComplete?.toSafeBoolean() ?: false
        mediaAccessApiKey = data.mediaAccessApiKey
        mediaAccessApiUrl = data.mediaAccessApiUrl

        difficulty = data.difficulty ?: ""
        difficultyName = data.difficultyName ?: ""
        playTime = data.playTime ?: ""
        calorie = data.calorie ?: ""
        bodyParts = data.bodyParts ?: ""
        bodyPartsName = data.bodyPartsName ?: ""
    }

    fun getDescription(context:Context):String{
        return "$bodyPartsName ・ ${playTime.secToMinTimeString()}${context.getString(R.string.unit_min)} ・ $calorie${context.getString(R.string.unit_kcal)}"
    }
}