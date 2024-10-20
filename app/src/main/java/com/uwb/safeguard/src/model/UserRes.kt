package com.uwb.safeguard.src.model

import com.google.gson.annotations.SerializedName
import com.uwb.safeguard.config.BaseResponse

data class UserRes (
    @SerializedName("uni_num") val uniNum : String,
    @SerializedName("user_x") val userX : Double,
    @SerializedName("user_y") val userY : Double,
    @SerializedName("user_dist") val userDist : Double,
    @SerializedName("user_lat") val userLat : Double,
    @SerializedName("user_lon") val userLon : Double,
    @SerializedName("user_flag") val userflag : Int
)