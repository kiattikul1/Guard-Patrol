package com.example.guard_patrol.Class

import com.google.gson.annotations.SerializedName

data class HomeClass(val data: GetPatrolPointData?)

data class GetPatrolPointData(val patrolHome: PatrolHome?)

data class PatrolHome(
    val workspace: String?,
    val patrols : ArrayList<Patrols>
)

data class Patrols(

    @SerializedName("points_id")
    val pointsId: String?,

    @SerializedName("point_name")
    val pointName: String?,

    val lat: String?,

    val lng: String?,
)