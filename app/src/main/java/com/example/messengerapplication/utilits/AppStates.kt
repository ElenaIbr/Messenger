package com.example.messengerapplication.utilits
import com.example.messengerapplication.utilits.*

enum class AppStates(val state: String) {
    ONLINE("В сети"),
    OFFLINE("Не в сети");

    companion object {
        fun updateStates(appState: AppStates){
            if(authFirebase.currentUser!=null){
                REF_DATABASE_ROOT.child(NODE_USERS).child(UID).child(CHILD_STATE)
                    .setValue(appState.state)
                    .addOnSuccessListener { USER.state = appState.state }
                    .addOnFailureListener { showToast(it.message.toString()) }
            }
        }
    }
}