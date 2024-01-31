package com.example.guard_patrol.Data.Interceptor

import android.content.Context
import com.example.guard_patrol.Data.Preference.TokenPref
import okhttp3.Interceptor
import okhttp3.Response

class AuthTokenInterceptor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val prefs = TokenPref(context)
        val token: String = prefs.getPreferences()
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
//        Log.e("TestTntercept", "Test Token $token")
        val request = requestBuilder.build()
        return chain.proceed(request)

    }
}