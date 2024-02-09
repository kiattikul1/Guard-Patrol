package com.example.guard_patrol.Class

import com.google.gson.annotations.SerializedName

data class HistoryDetailClass(
    val data : GetDetailHistoryData?
)

data class GetDetailHistoryData(
    val detailHistory : DetailHistory?
)

data class DetailHistory(

    val workspace : String?,

    @SerializedName("points_id")
    val pointsId : String?,

    @SerializedName("point_name")
    val pointName : String?,

    val tasks : ArrayList<Tasks>?
)

data class Tasks(
    val id: String?,

    @SerializedName("title_task")
    val titleTask: String?,

    val sops: ArrayList<SOPS>?,

    @SerializedName("is_normal")
    var isNormal: Boolean?,

    var evidenceImages: ArrayList<String?>,

    var remark: String?
)

data class SOPS(
    val id: String?,

    @SerializedName("title_sop")
    val titleSop: String?,

    val description: String?
)
