package com.uwb.safeguard.src;

import android.util.Log
import com.uwb.safeguard.config.ApplicationClass
import com.uwb.safeguard.src.model.UserDeleteReq
import com.uwb.safeguard.src.model.UserRes
import okhttp3.ResponseBody

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainService (private val mainActivityInterface : MainActivityInterface){

    fun tryPostUser(userRes: UserRes){
        val mainActivityRetrofitInterface = ApplicationClass.sRetrofit.create(
            MainActivityRetrofitInterface::class.java)
        mainActivityRetrofitInterface.postUser(userRes).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                mainActivityInterface.onPostUserSuccess(response.body() as ResponseBody)
                Log.i("POST 통신", "onResponse 호출됨")
                val userResponse = response.body()!!
                Log.i("POST 통신 성공", "UserResponse: $userResponse")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                mainActivityInterface.onPostUserFailure(t.message ?: "통신 오류")
                Log.e("POST 통신 오류", t.message ?: "통신 오류")
            }
        })
    }
    fun tryDeleteUser(uni_num: String){
        val mainActivityRetrofitInterface = ApplicationClass.sRetrofit.create(
            MainActivityRetrofitInterface::class.java)
        mainActivityRetrofitInterface.deleteUser(uni_num).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                mainActivityInterface.onDeleteUserSuccess(response.body() as ResponseBody)
                Log.i("DELETE 통신", "onResponse 호출됨")
                val userResponse = response.body()!!
                Log.i("DELETE 통신 성공", "UserResponse: $userResponse")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                mainActivityInterface.onDeleteUserFailure(t.message ?: "통신 오류")
                Log.e("Delete 통신 오류", t.message ?: "통신 오류")
            }
        })
    }
}
