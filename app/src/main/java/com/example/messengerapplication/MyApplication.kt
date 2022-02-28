package com.example.messengerapplication

import UserInteractor
import android.app.Application
import com.example.messengerapplication.features.chat.domain.ChatInteractor
import com.example.messengerapplication.features.user.domain.entity.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MyApplication : Application() {

    lateinit var authFb: FirebaseAuth
    lateinit var databaseFbRef: DatabaseReference
    lateinit var storageFbRef: StorageReference
    lateinit var currentUser: User
    lateinit var currentUserID: String
    lateinit var userInteractor: UserInteractor
    lateinit var chatInteractor: ChatInteractor

    override fun onCreate() {
        super.onCreate()
        authFb = FirebaseAuth.getInstance()
        databaseFbRef = FirebaseDatabase.getInstance().reference
        storageFbRef = FirebaseStorage.getInstance().reference

        userInteractor = UserInteractor()
        chatInteractor = ChatInteractor()
    }

}