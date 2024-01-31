package com.example.guard_patrol.Class

import com.google.gson.annotations.SerializedName

enum class CellType{
    TASK,BUTTON
}

data class TaskClass(
    var cellType: CellType,
    var data: GetTaskData?,
    var isNormal: Boolean? = null,
    var imageUrls: ArrayList<String?> = ArrayList(listOf(null)),
    var remark: String? = null
)

data class GetTaskData(val assignTask : AssignTask?)

data class AssignTask(val tasks : ArrayList<Task>?)

data class Task(
    val id: String?,

    @SerializedName("title_task")
    val titleTask: String?,

    val sops: ArrayList<SOP>?
)

data class SOP(
    val id: String?,

    @SerializedName("title_sop")
    val titleSop: String?,

    val description: String?
)