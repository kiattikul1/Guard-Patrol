package com.example.guard_patrol.Class

import com.google.gson.annotations.SerializedName

data class ProfileClass(var data: GetProfileData?)

data class GetProfileData(val selectProfile : SelectProfile?)

data class SelectProfile(
    val id: String?,

    val firstname: String?,

    val lastname: String?,

    val role: String?,

    val birthday: String?,

    @SerializedName("phone_number")
    val phoneNumber: String?,

    val email: String?,

    val username: String?,

    val password: String?,
)