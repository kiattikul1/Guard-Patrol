package com.example.guard_patrol.Data

import okhttp3.ResponseBody
import retrofit2.Converter

class Data<T> {
    var data: T? = null
}

class DataConverter<Any>(
    private val delegate: Converter<ResponseBody, Data<Any>>?
) : Converter<ResponseBody, Any> {
    override fun convert(value: ResponseBody): Any? {
        return try {
            val graphQLDataModel = delegate?.convert(value)
            graphQLDataModel?.data
        } catch (e: java.lang.Exception){
            null
        }
    }
}