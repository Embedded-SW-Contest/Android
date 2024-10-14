package com.uwb.safety.src.model

import com.google.gson.annotations.SerializedName
import com.uwb.safety.config.BaseResponse

data class UserRes (
    @SerializedName("userId") val userId : Long,
    @SerializedName("uni_num") val uniNum : String,
    @SerializedName("user_x") val userX : Double,
    @SerializedName("user_y") val userY : Double,
    @SerializedName("user_dist") val userDist : Double,
    @SerializedName("user_lat") val userLat : Double,
    @SerializedName("user_lon") val userLon : Double
) : BaseResponse()