package com.example.guard_patrol.Data.Service

import android.content.Context
import com.example.guard_patrol.Data.Interceptor.AuthTokenInterceptor
import com.example.guard_patrol.Data.Interface.GraphQLService
import com.example.guard_patrol.Data.Interface.RestAPIService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class AllService {
    companion object {
        private const val baseUrl = "http://10.2.98.228:4000/"
//        private const val baseUrl = "http://192.168.173.232:4000/"

        private var retrofitService: GraphQLService? = null
        private var retrofitServiceAPI: RestAPIService? = null
        private var gson = GsonBuilder()
            .setLenient()
            .create()

        private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        private var client: OkHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(logging)
            .build()

        fun initialize(context: Context) {
            client = OkHttpClient.Builder()
                .addInterceptor(AuthTokenInterceptor(context))
                .addNetworkInterceptor(logging)
                .build()
        }

        fun getInstance() : GraphQLService {
            retrofitService = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(DataConverterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build()
                .create(GraphQLService::class.java)
            return retrofitService!!
        }

        fun getURL() : RestAPIService {
            retrofitServiceAPI = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(DataConverterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build()
                .create(RestAPIService::class.java)
            return retrofitServiceAPI!!
        }

    }

}