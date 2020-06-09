package com.kakaovx.homet.tv.store.api.account

import com.google.gson.annotations.SerializedName

data class JWTData(
    @SerializedName("jwt") var jwt: String? = null
)