package com.uwb.safeguard.src.model

import com.google.gson.annotations.SerializedName

data class CarRes (
    @SerializedName("result") val result : List<CarResponse>
)