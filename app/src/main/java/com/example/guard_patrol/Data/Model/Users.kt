package com.example.guard_patrol.Data.Model

data class Users(
    var id:String,
    var firstName:String,
    var lastName:String,
    var username:String,
    var password:String,
    var birthDay:String,
    var role:String,
    var email:String,
    var phoneNumber:String,
    var createdAt:String,
    var updatedAt:String,
    var isActive:String,
)

//data class SigninDTO (
//    val token : String,
//    val userLogin : Users?
//)

//val userDetail = SigninDTO{
//    userDetail.users.id
//}
