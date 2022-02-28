package com.example.messengerapplication.utilits

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

