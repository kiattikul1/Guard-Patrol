package com.example.guard_patrol.Data.Interface

import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface GraphQLService {
    @POST("graphql")
    @Headers("Content-Type: application/json","Accept: */*")
    fun callGraphQLService(@Body req:JsonObject): Call<ResponseBody>
}