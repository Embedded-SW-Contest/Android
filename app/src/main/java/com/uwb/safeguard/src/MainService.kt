package com.uwb.safeguard.src;

import android.util.Log
import com.uwb.safeguard.config.ApplicationClass
import com.uwb.safeguard.src.model.UserRes
import com.uwb.safeguard.src.model.UserResponse

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainService (private val mainActivityInterface : MainActivityInterface){

    fun tryPostUser(userRes: UserRes){
        val mainActivityRetrofitInterface = ApplicationClass.sRetrofit.create(
            MainActivityRetrofitInterface::class.java)
        mainActivityRetrofitInterface.postUser(userRes).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                Log.i("POST 통신", "onResponse 호출됨")
                if (response.isSuccessful && response.body() != null) {
                    val userResponse = response.body()!!
                    Log.i("POST 통신 성공", "UserResponse: $userResponse")
                    // 성공적인 응답을 처리할 필요가 있으면 추가 작업 수행
                } else {
                    Log.i("POST 통신 실패", "응답이 성공적이지 않거나 본문이 비어있습니다.")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("POST 통신 오류", t.message ?: "통신 오류")
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
