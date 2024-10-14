package com.uwb.safety.src

import com.uwb.safety.src.model.CarRes
import com.uwb.safety.src.model.CarResponse
import com.uwb.safety.src.model.UserRes

interface MainActivityInterface {
    fun onGetCarSuccess(response: List<CarResponse>)

    fun onGetCarFailure(message: String)
}