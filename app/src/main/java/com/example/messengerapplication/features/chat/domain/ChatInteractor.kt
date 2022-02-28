package com.example.messengerapplication.features.chat.domain

import com.example.messengerapplication.features.chat.IChatInteractor
import com.example.messengerapplication.features.chat.domain.entity.CommonModel
import com.example.messengerapplication.utilits.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class ChatInteractor : IChatInteractor {

    override fun saveToChat(id: String, contName: String, type: String) {
        val refToCurUser = "$NODE_CHATLIST/${mApplication.currentUserID}/$id"
        val refToReceivUser = "$NODE_CHATLIST/$id/${mApplication.currentUserID}"

        val mapRefUser = hashMapOf<String, Any>()
        val mapRefReceivUser= hashMapOf<String, Any>()

        val currentTime = ServerValue.TIMESTAMP

        readMessageCount(object : IFirebaseCallback {
            override fun onCallback(value: Double?) {

                mapRefUser[CHILD_ID] = id
                mapRefUser[CHILD_TYPE] = type
                mapRefUser[CHILD_NAME_FROM_CONTACTS] = contName
                mapRefUser[CHILD_LAST_MESSAGE_TIME] = currentTime
                mapRefUser[CHILD_MESSAGE_COUNT] = 0

                mapRefReceivUser[CHILD_ID] = mApplication.currentUserID
                mapRefReceivUser[CHILD_TYPE] = type
                mapRefReceivUser[CHILD_NAME_FROM_CONTACTS] = mApplication.currentUser.fullname
                mapRefReceivUser[CHILD_LAST_MESSAGE_TIME] = currentTime
                mapRefReceivUser[CHILD_MESSAGE_COUNT] = messgeCount + 1

                val commonMap = hashMapOf<String, Any>()
                commonMap[refToCurUser] = mapRefUser
                commonMap[refToReceivUser] = mapRefReceivUser

                mApplication.databaseFbRef.updateChildren(commonMap)
                    .addOnFailureListener { showToast(it.message.toString()) }
            }
        }, id)
    }

    override fun saveChat(id: String, contName: String, timeStamp: Any) {
        val refUser = "$NODE_CHATLIST/${mApplication.currentUserID}/$id"
        val refReceivedUser = "$NODE_CHATLIST/$id/${mApplication.currentUserID}"
        val mapRefUser = hashMapOf<String, Any>()
        val mapRefReceivUser= hashMapOf<String, Any>()

        mapRefUser[CHILD_ID] = id
        mapRefUser[CHILD_NAME_FROM_CONTACTS] = contName
        mapRefUser[CHILD_LAST_MESSAGE_TIME] = timeStamp
        mapRefUser[CHILD_MESSAGE_COUNT] = 0

        mapRefReceivUser[CHILD_ID] = mApplication.currentUserID
        mapRefReceivUser[CHILD_NAME_FROM_CONTACTS] = mApplication.currentUser.fullname
        mapRefReceivUser[CHILD_LAST_MESSAGE_TIME] = timeStamp
        mapRefReceivUser[CHILD_MESSAGE_COUNT] = 0

        val commonMap = hashMapOf<String, Any>()
        commonMap[refUser] = mapRefUser
        commonMap[refReceivedUser] = mapRefReceivUser

        mApplication.databaseFbRef.updateChildren(commonMap)
            .addOnFailureListener { showToast(it.message.toString()) }
    }

    interface IFirebaseCallback {
        fun onCallback(value: Double?)
    }

    override fun readMessageCount(firebaseCallback : IFirebaseCallback, uid: String) {
        val nameRef = mApplication.databaseFbRef
        val eventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if(dataSnapshot.hasChild(NODE_CHATLIST) && dataSnapshot.child(NODE_CHATLIST).hasChild(uid)
                    && dataSnapshot.child(NODE_CHATLIST).child(uid).hasChild(mApplication.currentUserID)){
                    messgeCount = dataSnapshot
                        .child(NODE_CHATLIST)
                        .child(uid).child(mApplication.currentUserID)
                        .child("messageCount")
                        .getValue<Double>()!!

                    firebaseCallback.onCallback(messgeCount)
                }else {
                    firebaseCallback.onCallback(messgeCount)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                messgeCount = 0.0
                firebaseCallback.onCallback(messgeCount)
            }
        }
        nameRef.addListenerForSingleValueEvent(eventListener)
    }

    override fun readMessageCount2(firebaseCallback : IFirebaseCallback, uid: String) {
        val nameRef = mApplication.databaseFbRef
        val eventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if(dataSnapshot.hasChild(NODE_CHATLIST) && dataSnapshot.child(NODE_CHATLIST).hasChild(uid)
                    && dataSnapshot.child(NODE_CHATLIST).child(uid).hasChild(mApplication.currentUserID)){
                    messgeCount2 = dataSnapshot
                        .child(NODE_CHATLIST)
                        .child(mApplication.currentUserID).child(uid)
                        .child("messageCount")
                        .getValue<Double>()!!

                    firebaseCallback.onCallback(messgeCount2)
                }else {
                    firebaseCallback.onCallback(messgeCount2)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                messgeCount2 = 0.0
                firebaseCallback.onCallback(messgeCount2)
            }
        }
        nameRef.addListenerForSingleValueEvent(eventListener)
    }

    override fun deleteChat(id: String, function: () -> Unit) {
        mApplication.databaseFbRef.child(NODE_CHATLIST).child(mApplication.currentUserID).child(id)
            .removeValue()
            .addOnSuccessListener { function() }
            .addOnFailureListener { it.message.toString() }
    }

    override fun clearChat(id: String, function: () -> Unit) {
        mApplication.databaseFbRef.child(NODE_MESSAGES).child(mApplication.currentUserID).child(id)
            .removeValue()
            .addOnSuccessListener {
                mApplication.databaseFbRef.child(NODE_MESSAGES).child(id).child(mApplication.currentUserID)
                    .removeValue()
                    .addOnSuccessListener { function() }
                    .addOnFailureListener { it.message.toString() }
            }
            .addOnFailureListener { it.message.toString() }
    }

    override fun updateChatContacts(arrContacts: ArrayList<CommonModel>) {
        if(mApplication.authFb.currentUser!=null){
            mApplication.databaseFbRef.child(NODE_PHONES).addListenerForSingleValueEvent(
                AppValueEventListener{
                    it.children.forEach { snapshop ->
                        arrContacts.forEach { contact ->
                            if(contact.phone == snapshop.key && contact.phone!= mApplication.currentUser.phone){
                                mApplication.databaseFbRef.child(NODE_PHONES_CONTACTS)
                                    .child(mApplication.currentUserID)
                                    .child(snapshop.value.toString()).child(CHILD_ID)
                                    .setValue(snapshop.value.toString())
                                    .addOnFailureListener { showToast(it.message.toString()) }

                                mApplication.databaseFbRef
                                    .child(NODE_PHONES_CONTACTS)
                                    .child(mApplication.currentUserID)
                                    .child(snapshop.value.toString())
                                    .child(CHILD_FULLNAME)
                                    .setValue(contact.fullname)
                                    .addOnFailureListener { showToast(it.message.toString()) }

                                mApplication.databaseFbRef.child(
                                    NODE_PHONES_CONTACTS
                                ).child(mApplication.currentUserID)
                                    .child(snapshop.value.toString())
                                    .child(CHILD_FULLNAME_LOWCASE)
                                    .setValue(contact.fullname.lowercase())
                                    .addOnFailureListener { showToast(it.message.toString()) }
                            }
                        }
                    }
                })
        }
    }
}
