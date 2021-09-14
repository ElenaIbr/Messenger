package com.example.messengerapplication.models

import java.util.*

data class CommonModel(

    //user
    val id: String = "",
    var username: String = "",
    var bio: String = "",
    var fullname: String = "",
    var fullnameLowcase: String = "",
    var state: String = "",
    var phone: String = "",
    var photoUrl: String = "",
    var namefromcontacts: String = "",
    var token: String = "",

    //messages
    var text: String = "",
    var type: String = "",
    var from: String = "",
    var timeStamp: Any = "",
    var messageStatus: String = "Не прочитано",
    var lastMessageTime: Any = "",
    var resLastMessageTime: Date = Date(0),
    var lastMessage: String = "",
    var messageCount: Int = 0

){
    override fun equals(other: Any?): Boolean {
        return (other as CommonModel).id == id
    }
}