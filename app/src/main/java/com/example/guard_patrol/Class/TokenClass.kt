package com.example.guard_patrol.Class

data class TokenClass(val data: GetTokenData?)

data class GetTokenData(val userLogin: UserLogin?)

data class UserLogin(val token: String?)
