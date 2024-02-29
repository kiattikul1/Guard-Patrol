package com.example.guard_patrol.Class

import com.google.gson.annotations.SerializedName

data class ContactListClass(var data: GetContactListData?)

data class GetContactListData(val authenticationPhone : ArrayList<AuthenticationPhone>?)

data class AuthenticationPhone(

    val firstname: String?,

    val lastname: String?,

    @SerializedName("phone_number")
    val phoneNumber: String?,

    val role: String?,
)