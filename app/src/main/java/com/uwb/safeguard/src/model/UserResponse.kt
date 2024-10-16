package com.uwb.safeguard.src.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("result") val result : List<UserRes>
)