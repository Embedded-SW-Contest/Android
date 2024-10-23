package com.uwb.safeguard.config

import android.app.Application
import android.content.SharedPreferences
import com.estimote.uwb.api.EstimoteUWBFactory
import com.uwb.safeguard.src.model.CarInfo
import okhttp3.Interceptor
import com.uwb.safeguard.src.model.CarResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApplicationClass : Application() {
    val API_URL = "https://uwb-safeguard.shop/"

    companion object {
        lateinit var sSharedPreferences: SharedPreferences
        lateinit var editor : SharedPreferences.Editor
        val X_ACCESS_TOKEN = "X-ACCESS-TOKEN"
        val USER_IDX = "USER_IDX"
        val USER_X = "USER_X"
        val USER_Y = "USER_Y"
        val USER_DIST = "USER_DIST"
        val CAR_LAT = "CAR_LAT"
        val CAR_LON = "CAR_LON"
        //val uwbManager = EstimoteUWBFactory.create()
        lateinit var carInfo : CarInfo

        lateinit var sRetrofit: Retrofit

    }
    override fun onCreate() {
        super.onCreate()
        sSharedPreferences =
            applicationContext.getSharedPreferences("USER_TOKEN", MODE_PRIVATE)
        editor = sSharedPreferences.edit()
        editor.putLong(USER_IDX,15)
        carInfo = CarInfo(0,0.0,0.0,"",0.0, 0.0)
        // 레트로핏 인스턴스 생성
        initRetrofitInstance()
    }

    private fun initRetrofitInstance() {
        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(5000, TimeUnit.MILLISECONDS)
            .connectTimeout(5000, TimeUnit.MILLISECONDS)
            // 로그캣에 okhttp.OkHttpClient로 검색하면 http 통신 내용을 보여줍니다.
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addNetworkInterceptor(XAccessTokenInterceptor()) // JWT 자동 헤더 전송
            .build()

        // sRetrofit 이라는 전역변수에 API url, 인터셉터, Gson을 넣어주고 빌드해주는 코드
        // 이 전역변수로 http 요청을 서버로 보내면 됩니다.
        sRetrofit = Retrofit.Builder()
            .baseUrl(API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}