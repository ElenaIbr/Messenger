package com.example.messengerapplication.utilits

import android.net.Uri
import android.provider.ContactsContract
import com.example.messengerapplication.models.CommonModel
import com.example.messengerapplication.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlin.collections.ArrayList

lateinit var authFirebase: FirebaseAuth
lateinit var REF_DATABASE_ROOT: DatabaseReference
lateinit var USER: User
lateinit var UID: String
lateinit var REF_STORAGE_ROOT: StorageReference

const val TYPE_TEXT = "text"

const val NODE_USERS = "users"
const val NODE_USERNAMES = "usernames"
const val NODE_PHONES = "phones"
const val NODE_PHONES_CONTACTS = "phone_contacts"
const val NODE_MESSAGES = "messages"
const val NODE_CHATLIST = "chatlist"


const val CHILD_ID = "id"
const val CHILD_PHONE = "phone"
const val CHILD_USERNAME = "username"
const val CHILD_FULLNAME = "fullname"
const val CHILD_PHOTO_URL = "photoUrl"
const val CHILD_STATE = "state"
const val CHILD_FULLNAME_LOWCASE = "fullnameLowcase"
const val CHILD_NAME_FROM_CONTACTS = "namefromcontacts"
const val CHILD_BIO = "bio"
const val CHILD_MESSAGE_STATUS= "messageStatus"

const val CHILD_TOKEN = "token"

const val CHILD_TEXT = "text"
const val CHILD_TYPE = "type"
const val CHILD_FROM = "from"
const val CHILD_TIMESTAMP = "timeStamp"

const val CHILD_LAST_MESSAGE_TIME = "lastMessageTime"

const val FOLDER_PROFILE_IMG = "profile_images"



fun initFirebase(){

    authFirebase = FirebaseAuth.getInstance()
    authFirebase.firebaseAuthSettings.setAppVerificationDisabledForTesting(true);
    REF_DATABASE_ROOT = FirebaseDatabase.getInstance().reference
    USER = User()
    UID = authFirebase.currentUser?.uid.toString()
    REF_STORAGE_ROOT = FirebaseStorage.getInstance().reference

}

fun updateField(newValue: String, field: String){
    REF_DATABASE_ROOT.child(NODE_USERS).child(UID).child(field)
        .setValue(newValue)
        .addOnCompleteListener {
            if(!it.isSuccessful) {
                showToast("Обновлено")
            }
        }
    if(field == CHILD_FULLNAME){
        REF_DATABASE_ROOT.child(NODE_USERS).child(UID).child(CHILD_FULLNAME_LOWCASE)
            .setValue(newValue.lowercase())
            .addOnCompleteListener {
                if(!it.isSuccessful) {
                    showToast("Обновлено")
                }
            }
    }
}

fun putUrlToDatabase(url: String, function: () -> Unit) {
    REF_DATABASE_ROOT.child(NODE_USERS).child(UID)
        .child(CHILD_PHOTO_URL).setValue(url)
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

fun initUser(function: () -> Unit){
    REF_DATABASE_ROOT.child(NODE_USERS).child(UID)
        .addListenerForSingleValueEvent(AppValueEventListener{
            USER = it.getValue(User::class.java) ?:User()

            if(USER.username=="") USER.username = UID
            function()
        })
}

fun initContacts() {
    if(checkPermission(READ_CONTACTS)){
        val arrContacts = arrayListOf<CommonModel>()
        val cursor = APP_ACTIVITY.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null)
        cursor?.let {
            while(it.moveToNext()){
                val fullName = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val phone = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val newModel = CommonModel()
                newModel.fullname = fullName
                newModel.fullnameLowcase = fullName.lowercase()
                newModel.phone = phone.replace(Regex("[\\s,-]"), "")
                arrContacts.add(newModel)
            }
        }
        cursor?.close()
        updateContactList(arrContacts)
    }
}

fun updateContactList(arrContacts: ArrayList<CommonModel>) {
    if(authFirebase.currentUser!=null){
        REF_DATABASE_ROOT.child(NODE_PHONES).addListenerForSingleValueEvent(AppValueEventListener{
            it.children.forEach { snapshop ->
                arrContacts.forEach { contact ->
                    if(contact.phone == snapshop.key && contact.phone!=USER.phone){
                        REF_DATABASE_ROOT.child(NODE_PHONES_CONTACTS).child(UID)
                            .child(snapshop.value.toString()).child(CHILD_ID)
                            .setValue(snapshop.value.toString())
                            .addOnFailureListener { showToast(it.message.toString()) }

                        REF_DATABASE_ROOT.child(NODE_PHONES_CONTACTS).child(UID)
                            .child(snapshop.value.toString()).child(CHILD_FULLNAME)
                            .setValue(contact.fullname)
                            .addOnFailureListener { showToast(it.message.toString()) }

                        REF_DATABASE_ROOT.child(NODE_PHONES_CONTACTS).child(UID)
                            .child(snapshop.value.toString()).child(CHILD_FULLNAME_LOWCASE)
                            .setValue(contact.fullname.lowercase())
                            .addOnFailureListener { showToast(it.message.toString()) }
                    }
                }
            }
        })
    }
}

fun saveToChatlist(
    id: String,
    contName: String,
    type: String
) {
    val refUser = "$NODE_CHATLIST/$UID/$id"
    val refReceivedUser = "$NODE_CHATLIST/$id/$UID"

    val mapRefUser = hashMapOf<String, Any>()
    val mapRefReceivUser= hashMapOf<String, Any>()

    val currentTime = ServerValue.TIMESTAMP

    //val date = LocalDate.parse(currentTime, DateTimeFormatter.ISO_DATE)


    mapRefUser[CHILD_ID] = id
    mapRefUser[CHILD_TYPE] = type
    mapRefUser[CHILD_NAME_FROM_CONTACTS] = contName
    mapRefUser[CHILD_LAST_MESSAGE_TIME] = currentTime


    mapRefReceivUser[CHILD_ID] = UID
    mapRefReceivUser[CHILD_TYPE] = type
    mapRefReceivUser[CHILD_NAME_FROM_CONTACTS] = USER.fullname
    mapRefReceivUser[CHILD_LAST_MESSAGE_TIME] = currentTime

    val commonMap = hashMapOf<String, Any>()
    commonMap[refUser] = mapRefUser
    commonMap[refReceivedUser] = mapRefReceivUser

    REF_DATABASE_ROOT.updateChildren(commonMap)
        .addOnFailureListener { showToast("${it.message.toString()}") }

}

fun deleteChat(id: String, function: () -> Unit) {
    REF_DATABASE_ROOT.child(NODE_CHATLIST).child(UID).child(id)
        .removeValue()
        .addOnSuccessListener { function() }
        .addOnFailureListener { it.message.toString() }
}

fun clearChat(id: String, function: () -> Unit) {
    REF_DATABASE_ROOT.child(NODE_MESSAGES).child(UID).child(id)
        .removeValue()
        .addOnSuccessListener {
            REF_DATABASE_ROOT.child(NODE_MESSAGES).child(id).child(UID)
                .removeValue()
                .addOnSuccessListener { function() }
                .addOnFailureListener { it.message.toString() }
        }
        .addOnFailureListener { it.message.toString() }
}
