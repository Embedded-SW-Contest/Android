package com.uwb.safeguard.src

import com.uwb.safeguard.src.model.CarResponse
import com.uwb.safeguard.src.model.UserRes
import com.uwb.safeguard.src.model.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MainActivityRetrofitInterface {
    @GET("api/cars")
    fun getCar(): Call<List<CarResponse>>

    @POST("api/users")
    fun postUser(@Body userRes: UserRes) : Call<UserResponse>



//    @GET("/crews")
//    fun getFewCrews(@Path("uninum") uninum:Long): Call<GetCrewRes>
}