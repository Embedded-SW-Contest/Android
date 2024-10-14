package com.uwb.safety.src;

import com.uwb.safety.config.ApplicationClass
import com.uwb.safety.src.model.CarRes
import com.uwb.safety.src.model.CarResponse
import com.uwb.safety.src.model.UserRes

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainService (val mainActivityInterface : MainActivityInterface){

    fun tryGetCar(){
        val mainActivityRetrofitInterface = ApplicationClass.sRetrofit.create(
            MainActivityRetrofitInterface::class.java)
        mainActivityRetrofitInterface.getCar().enqueue(object : Callback<List<CarResponse>> {
            override fun onResponse(call: Call<List<CarResponse>>, response: Response<List<CarResponse>>) {
                mainActivityInterface.onGetCarSuccess(response.body() as List<CarResponse>)
            }

            override fun onFailure(call: Call<List<CarResponse>>, t: Throwable) {
                mainActivityInterface.onGetCarFailure(t.message ?: "통신 오류")
            }
        })
    }

//    fun tryGetFewCrews(){
//        val mainFragmentRetrofitInterface = ApplicationClass.sRetrofit.create(
//                MainFragmentRetrofitInterface::class.java)
//        mainFragmentRetrofitInterface.getFewCrews().enqueue(object : Callback<GetCrewRes> {
//            override fun onResponse(call: Call<GetCrewRes>, response: Response<GetCrewRes>) {
//                mainFragmentInterface.onGetFewCrewSuccess(response.body() as GetCrewRes)
//            }
//
//            override fun onFailure(call: Call<GetCrewRes>, t: Throwable) {
//                mainFragmentInterface.onGetFewCrewFailure(t.message ?: "통신 오류")
//            }
//        })
//    }
}
