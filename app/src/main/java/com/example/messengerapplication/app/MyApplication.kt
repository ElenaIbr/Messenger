package com.example.messengerapplication.app

import android.app.Application
import com.example.messengerapplication.models.User
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

    override fun onCreate() {
        super.onCreate()
        authFb = FirebaseAuth.getInstance()
        databaseFbRef = FirebaseDatabase.getInstance().reference
        storageFbRef = FirebaseStorage.getInstance().reference
    }

}