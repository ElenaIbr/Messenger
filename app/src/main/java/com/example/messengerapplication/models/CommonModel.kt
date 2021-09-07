package com.example.messengerapplication.models

data class CommonModel(
    val id: String = "",
    var username: String = "",
    var bio: String = "",
    var fullname: String = "",
    var fullnameLowcase: String = "",
    var state: String = "",
    var phone: String = "",
    var photoUrl: String = "",

    //messages
    var text: String = "",
    var type: String = "",
    var from: String = "",
    var timeStamp: Any = "",

    var lastMessage: String = "",
    var namefromcontacts: String = ""



){
    override fun equals(other: Any?): Boolean {
        return (other as CommonModel).id == id
    }
}