package com.uwb.safety.config

import com.uwb.safety.config.ApplicationClass.Companion.ADDRESS_KEY
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.jvm.Throws

class KakaoAccessInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request().newBuilder()
        builder.addHeader("Authorization","KakaoAK " + ADDRESS_KEY)
        return chain.proceed(builder.build())
    }
}