package com.example.guard_patrol.Class

import android.util.JsonReader
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class ScanQrClass(val data: GetScanData?)

data class GetScanData(val scanQrcode: ScanQrcode?)

data class ScanQrcode(

    val message: String?,

    @SerializedName("workspace_id")
    val workspaceId: String?,

    @SerializedName("point_id")
    val pointId: String?,

    @SerializedName("point_name")
    val pointName: String?,

    val lat: String?,

    val lng: String?,

    )
