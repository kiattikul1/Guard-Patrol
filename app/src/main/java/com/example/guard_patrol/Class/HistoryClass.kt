package com.example.guard_patrol.Class

import com.google.gson.annotations.SerializedName

data class HistoryClass(
    var dateSelect: String? = null,
    var data: GetHistoryData?,
)

data class GetHistoryData(
    val history: GetLocation?
)

data class GetLocation(
    val workspace: String?,
    val patrols: ArrayList<Patrol>?
)

data class Patrol(

    @SerializedName("points_id")
    val pointsId: String?,

    @SerializedName("point_name")
    val pointName: String?,

    @SerializedName("process_date")
    val processDate: String?,

    @SerializedName("completed_at")
    val completedAt: String?,
)