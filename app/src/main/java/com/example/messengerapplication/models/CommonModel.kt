package com.example.messengerapplication.models

data class CommonModel(
    val id: String = "",
    var username: String = "",
    var boi: String = "",
    var fullname: String = "",
    var state: String = "",
    var phone: String = "",
    var photoUrl: String = "",

    //messages
    var text: String = "",
    var type: String = "",
    var from: String = "",
    var timeStamp: Any = "")