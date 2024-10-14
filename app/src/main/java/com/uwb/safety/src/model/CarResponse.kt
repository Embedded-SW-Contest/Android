package com.uwb.safety.src.model

import com.google.gson.annotations.SerializedName
import com.uwb.safety.config.BaseResponse

data class CarResponse (
    @SerializedName("car_id") val car_id : Long,
    @SerializedName("car_lat") val car_lat : Double,
    @SerializedName("car_lon") val car_lon : Double,
    @SerializedName("uni_num") val uni_num : String,
    @SerializedName("braking_distance") val braking_distance : Double
)