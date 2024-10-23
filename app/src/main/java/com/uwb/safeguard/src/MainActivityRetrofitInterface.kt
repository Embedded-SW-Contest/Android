package com.uwb.safeguard.src

import com.uwb.safeguard.src.model.UserRes
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface MainActivityRetrofitInterface {

    @POST("api/users")
    fun postUser(@Body userRes: UserRes) : Call<ResponseBody>

    @DELETE("api/users/{uni_num}")
    fun deleteUser(@Path("uni_num") uni_num: String) : Call<ResponseBody>

}