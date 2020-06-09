package com.kakaovx.homet.tv.store.api.homet

import com.google.gson.annotations.SerializedName



data class CategoryData(
    @SerializedName("codeId") var codeId: String? = null,
    @SerializedName("codeName") var codeName: String? = null
)

data class ProgramList(
    @SerializedName("totalCount") var totalCount: String? = null,
    @SerializedName("programs") var programs: ArrayList<ProgramData>? = null
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
)

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
    @SerializedName("isMultiView") val isMultiView: Boolean? = null,
    @SerializedName("movieType") val movieType: String?,
    @SerializedName("movieTypeName") val movieTypeName: String?
)


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
    @SerializedName("isMultiView") val isMultiView: Boolean? = null
)
