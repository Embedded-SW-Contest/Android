package com.uwb.safeguard.src

import com.uwb.safeguard.src.model.CarResponse

interface MainActivityInterface {
    fun onGetCarSuccess(response: List<CarResponse>)

    fun onGetCarFailure(message: String)

    fun onDeleteUserSuccess(response: String)

    fun onDeleteUserFailure(message: String)
}