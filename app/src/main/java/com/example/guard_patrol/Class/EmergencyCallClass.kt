package com.example.guard_patrol.Class

import com.google.gson.annotations.SerializedName

data class EmergencyCallClass(
    var data: GetEmergencyCallData?,
)

data class GetEmergencyCallData(
    val selectEmergency : ArrayList<SelectEmergency>?
)

data class SelectEmergency(

    val id: String?,

    val title: String?,

    @SerializedName("phone_number")
    val phoneNumber: String?,
)
