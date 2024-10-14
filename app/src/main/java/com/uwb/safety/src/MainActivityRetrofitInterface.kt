package com.uwb.safety.src

import com.uwb.safety.src.model.CarRes
import com.uwb.safety.src.model.CarResponse
import com.uwb.safety.src.model.UserRes
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface MainActivityRetrofitInterface {
    @GET("api/cars")
    fun getCar(): Call<List<CarResponse>>

//    @GET("/crews")
//    fun getFewCrews(@Path("uninum") uninum:Long): Call<GetCrewRes>
}