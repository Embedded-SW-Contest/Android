package com.uwb.safeguard.src.model

import com.google.gson.annotations.SerializedName

data class UserDeleteReq (
    @SerializedName("uni_num") val uniNum : String
)