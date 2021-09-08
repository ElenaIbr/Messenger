package com.example.messengerapplication.models

import java.util.*

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

    var lastMessageTime: Any = "",
    var resLastMessageTime: Date = Date(0),
    //var resLastMessageTime: Date = Date(0),

    var lastMessage: String = "",
    var namefromcontacts: String = ""



){
    override fun equals(other: Any?): Boolean {
        return (other as CommonModel).id == id
    }
}