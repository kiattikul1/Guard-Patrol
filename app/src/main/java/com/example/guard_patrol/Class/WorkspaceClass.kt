package com.example.guard_patrol.Class

import com.google.gson.annotations.SerializedName

data class WorkspaceClass(val data: GetWorkspaceData?)

data class GetWorkspaceData(val performTask: PerformTask?)

data class PerformTask(
    @SerializedName("workspace_all")
    val workspaceAll: ArrayList<WorkspaceItem>?,
    @SerializedName("workspace_today")
    val workspaceToday: ArrayList<WorkspaceItem>?
)

data class WorkspaceItem(
    val id: String?,
    val workspace: String?,
    val status: Boolean? = false
)