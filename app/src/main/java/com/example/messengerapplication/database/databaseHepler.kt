package com.example.messengerapplication.utilits

import android.net.Uri
import android.provider.ContactsContract
import com.example.messengerapplication.models.CommonModel
import com.example.messengerapplication.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.StorageReference

//Nodes
const val NODE_USERS = "users"
const val NODE_USERNAMES = "usernames"
const val NODE_PHONES = "phones"
const val NODE_PHONES_CONTACTS = "phone_contacts"
const val NODE_MESSAGES = "messages"
const val NODE_CHATLIST = "chatlist"

//User node
const val CHILD_ID = "id"
const val CHILD_PHONE = "phone"
const val CHILD_USERNAME = "username"
const val CHILD_FULLNAME = "fullname"
const val CHILD_PHOTO_URL = "photoUrl"
const val CHILD_STATE = "state"
const val CHILD_FULLNAME_LOWCASE = "fullnameLowcase"
const val CHILD_NAME_FROM_CONTACTS = "namefromcontacts"
const val CHILD_BIO = "bio"
const val CHILD_TOKEN = "token"

//Message node
const val CHILD_MESSAGE_STATUS= "messageStatus"
const val CHILD_MESSAGE_COUNT = "messageCount"
const val CHILD_TEXT = "text"
const val CHILD_TYPE = "type"
const val CHILD_FROM = "from"
const val CHILD_TIMESTAMP = "timeStamp"
const val CHILD_LAST_MESSAGE_TIME = "lastMessageTime"

//Storage folder
const val FOLDER_PROFILE_IMG = "profile_images"

const val TYPE_TEXT = "text"
const val FROM_CHAT = "fromChat"

lateinit var contactNamesFromDevice : MutableMap<String, String>

var messgeCount: Double = 0.0
var messgeCount2: Double = 0.0

fun updateDbValue(newValue: String, field: String){
    mApplication.databaseFbRef.child(NODE_USERS).child(mApplication.currentUserID).child(field)
        .setValue(newValue)
        .addOnCompleteListener {
            if(!it.isSuccessful) {
                showToast("Updated")
            }
        }
    if(field == CHILD_FULLNAME){
        mApplication.databaseFbRef.child(NODE_USERS).child(mApplication.currentUserID).child(
            CHILD_FULLNAME_LOWCASE
        )
            .setValue(newValue.lowercase())
            .addOnCompleteListener {
                if(!it.isSuccessful) {
                    showToast("Updated")
                }
            }
    }
}

fun putUrlToDb(url: String, function: () -> Unit) {
    mApplication.databaseFbRef.child(NODE_USERS).child(mApplication.currentUserID).child(CHILD_PHOTO_URL)
        .setValue(url)
        .addOnCompleteListener { function() }
        .addOnFailureListener { showToast(it.message.toString()) }
}

fun getUrlFromStorage(path: StorageReference, function: (url: String) -> Unit) {
    path.downloadUrl
        .addOnCompleteListener { function(it.result.toString()) }
        .addOnFailureListener { showToast(it.message.toString()) }
}

fun putImageToStorage(uri: Uri, path: StorageReference, function: () -> Unit) {
    path.putFile(uri)
        .addOnCompleteListener { function() }
        .addOnFailureListener { showToast(it.message.toString()) }
}

fun deleteChat(id: String, function: () -> Unit) {
    mApplication.databaseFbRef.child(NODE_CHATLIST).child(mApplication.currentUserID).child(id)
        .removeValue()
        .addOnSuccessListener { function() }
        .addOnFailureListener { it.message.toString() }
}

fun clearChat(id: String, function: () -> Unit) {
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

fun updateChatContacts(arrContacts: ArrayList<CommonModel>) {
    if(mApplication.authFb.currentUser!=null){
        mApplication.databaseFbRef.child(NODE_PHONES).addListenerForSingleValueEvent(
            AppValueEventListener{
                it.children.forEach { snapshop ->
                    arrContacts.forEach { contact ->
                        if(contact.phone == snapshop.key && contact.phone!= mApplication.currentUser.phone){
                            mApplication.databaseFbRef.child(NODE_PHONES_CONTACTS).child(mApplication.currentUserID)
                                .child(snapshop.value.toString()).child(CHILD_ID)
                                .setValue(snapshop.value.toString())
                                .addOnFailureListener { showToast(it.message.toString()) }

                            mApplication.databaseFbRef.child(
                                NODE_PHONES_CONTACTS
                            ).child(mApplication.currentUserID)
                                .child(snapshop.value.toString()).child(CHILD_FULLNAME)
                                .setValue(contact.fullname)
                                .addOnFailureListener { showToast(it.message.toString()) }

                            mApplication.databaseFbRef.child(
                                NODE_PHONES_CONTACTS
                            ).child(mApplication.currentUserID)
                                .child(snapshop.value.toString()).child(CHILD_FULLNAME_LOWCASE)
                                .setValue(contact.fullname.lowercase())
                                .addOnFailureListener { showToast(it.message.toString()) }
                        }
                    }
                }
            })
    }
}

fun initCurrentUser(function: () -> Unit){
    mApplication.databaseFbRef.child(NODE_USERS).child(mApplication.currentUserID)
        .addListenerForSingleValueEvent(AppValueEventListener{
            mApplication.currentUser = it.getValue(User::class.java) ?: User()
            function()
        })
}

fun initUserContacts() {
    if(checkPermission(READ_CONTACTS)){
        val contactsFromDevice = arrayListOf<CommonModel>()
        val cursor = appActivity.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null)
        contactNamesFromDevice = mutableMapOf()
        cursor?.let {
            while(it.moveToNext()){
                val fullName = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val phone = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val initialModel = CommonModel()
                initialModel.fullname = fullName
                initialModel.fullnameLowcase = fullName.lowercase()
                initialModel.phone = phone.replace(Regex("[\\s,-]"), "")
                contactsFromDevice.add(initialModel)

                contactNamesFromDevice.put(initialModel.phone, fullName)
            }
        }
        cursor?.close()
        updateChatContacts(contactsFromDevice)
    }
}

fun saveToChatlist(
    id: String,
    contName: String,
    type: String
) {
    val refToCurUser = "$NODE_CHATLIST/${mApplication.currentUserID}/$id"
    val refToReceivUser = "$NODE_CHATLIST/$id/${mApplication.currentUserID}"

    val mapRefUser = hashMapOf<String, Any>()
    val mapRefReceivUser= hashMapOf<String, Any>()

    val currentTime = ServerValue.TIMESTAMP

    readData(object : firebaseCallback {
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

fun saveToChatlist(
    id: String,
    contName: String
) {
    val refUser = "$NODE_CHATLIST/${mApplication.currentUserID}/$id"
    val refReceivedUser = "$NODE_CHATLIST/$id/${mApplication.currentUserID}"
    val mapRefUser = hashMapOf<String, Any>()
    val mapRefReceivUser= hashMapOf<String, Any>()
    val currentTime = ServerValue.TIMESTAMP

    mapRefUser[CHILD_ID] = id
    mapRefUser[CHILD_NAME_FROM_CONTACTS] = contName
    mapRefUser[CHILD_LAST_MESSAGE_TIME] = currentTime
    mapRefUser[CHILD_MESSAGE_COUNT] = 0

    mapRefReceivUser[CHILD_ID] = mApplication.currentUserID
    mapRefReceivUser[CHILD_NAME_FROM_CONTACTS] = mApplication.currentUser.fullname
    mapRefReceivUser[CHILD_LAST_MESSAGE_TIME] = currentTime
    mapRefReceivUser[CHILD_MESSAGE_COUNT] = 0

    val commonMap = hashMapOf<String, Any>()
    commonMap[refUser] = mapRefUser
    commonMap[refReceivedUser] = mapRefReceivUser

    mApplication.databaseFbRef.updateChildren(commonMap)
        .addOnFailureListener { showToast(it.message.toString()) }
}

interface firebaseCallback {
    fun onCallback(value: Double?)
}

fun readData(firebaseCallback : firebaseCallback, uid: String){
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

fun readData2(firebaseCallback : firebaseCallback, uid: String){
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



