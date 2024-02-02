package com.example.guard_patrol.Data.Interface

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


//interface RestAPIService {
//    @POST("upload")
//    @Headers("Content-Type: application/json","Accept: */*")
//    fun callRestAPIService(@Body body: RequestBody): Call<ResponseBody>
//}

interface RestAPIService {
    @Multipart
    @POST("upload")
    @Headers("Accept: */*")
    fun callRestAPIService(@Part imagePart: List<MultipartBody.Part>): Call<ResponseBody>
}