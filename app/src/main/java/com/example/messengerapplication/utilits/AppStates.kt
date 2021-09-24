package com.example.messengerapplication.utilits

import android.util.Log

enum class AppStates(val state: String) {
    ONLINE("online"),
    OFFLINE("offline");

    companion object {
        fun updateStates(appState: AppStates) {
            if (mApplication.authFb.currentUser != null) {

                Log.d("MyLog", "$appState")

                mApplication.databaseFbRef.child(NODE_USERS).child(mApplication.currentUserID)
                    .child(CHILD_STATE)
                    .setValue(appState.state)
                    .addOnSuccessListener { mApplication.currentUser.state = appState.state }
                    .addOnFailureListener { showToast(it.message.toString()) }
            }
        }
    }
}