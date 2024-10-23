package com.uwb.safeguard.src

import com.uwb.safeguard.src.model.CarResponse
import okhttp3.ResponseBody

interface MainActivityInterface {
    fun onGetCarSuccess(response: List<CarResponse>)

    fun onGetCarFailure(message: String)

    fun onDeleteUserSuccess(response: ResponseBody)

    fun onDeleteUserFailure(message: String)

    fun onPostUserSuccess(response: ResponseBody)

    fun onPostUserFailure(message: String)
}