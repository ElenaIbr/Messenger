package com.example.messengerapplication.features.chat

import com.example.messengerapplication.features.chat.domain.ChatInteractor
import com.example.messengerapplication.features.chat.domain.entity.CommonModel

interface IChatInteractor {

    fun saveToChat(id: String, contName: String, type: String)

    fun saveChat(id: String, contName: String, timeStamp: Any)

    fun readMessageCount(firebaseCallback : ChatInteractor.IFirebaseCallback, uid: String)

    fun readMessageCount2(firebaseCallback : ChatInteractor.IFirebaseCallback, uid: String)

    fun deleteChat(id: String, function: () -> Unit)

    fun clearChat(id: String, function: () -> Unit)

    fun updateChatContacts(arrContacts: ArrayList<CommonModel>)

}

