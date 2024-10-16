package com.uwb.safeguard.src.model

import com.google.gson.annotations.SerializedName

data class CarResponse (
    @SerializedName("car_id") var car_id : Long,
    @SerializedName("car_lat") var car_lat : Double,
    @SerializedName("car_lon") var car_lon : Double,
    @SerializedName("uni_num") var uni_num : String,
    @SerializedName("braking_distance") var braking_distance : Double
)