package com.uwb.safety.src.model

import com.google.gson.annotations.SerializedName
import com.uwb.safety.config.BaseResponse

data class CarRes (
    @SerializedName("result") val result : List<CarResponse>
)