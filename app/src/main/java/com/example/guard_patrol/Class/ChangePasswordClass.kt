package com.example.guard_patrol.Class

data class ChangePasswordClass(var data: GetStatus?)

data class GetStatus(var changePassword: ChangePassword?)

data class ChangePassword(
    var status: String?,
    var massager: String?,
)