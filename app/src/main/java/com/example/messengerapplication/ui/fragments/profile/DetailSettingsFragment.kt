package com.example.messengerapplication.ui.fragments.profile

import com.example.messengerapplication.app.MyApplication
import com.example.messengerapplication.databinding.FragmentDetailSettingsBinding
import com.example.messengerapplication.ui.fragments.BaseFragment
import com.example.messengerapplication.utilits.*

class DetailSettingsFragment() : BaseFragment<FragmentDetailSettingsBinding>() {

    private var isUsername: Boolean? = null

    override fun getViewBinding() = FragmentDetailSettingsBinding.inflate(layoutInflater)

    private lateinit var mApplication: MyApplication

    override fun onStart() {
        super.onStart()

        mApplication = (appActivity.application as MyApplication)

        binding.usernameInput.setText(mApplication.currentUser.username)
        binding.fullnameInput.setText(mApplication.currentUser.fullname)
        binding.bioInput.setText(mApplication.currentUser.bio)
        binding.saveSettingsBtn

        isUsername = false

        binding.saveSettingsBtn.setOnClickListener {
            var changingsCount: Byte = 0
            if (binding.usernameInput.text.toString() != mApplication.currentUser.username) {
                mApplication.currentUser.username = binding.usernameInput.text.toString()
                isUsername = true
                changeName(SettingsType.USERNAME, mApplication.currentUser.username)
                changingsCount++
            }
            if (binding.fullnameInput.text.toString() != mApplication.currentUser.fullname) {
                mApplication.currentUser.fullname = binding.fullnameInput.text.toString()
                changeName(SettingsType.FULLNAME, mApplication.currentUser.fullname)
                changingsCount++
            }
            if (binding.bioInput.text.toString() != mApplication.currentUser.bio) {
                mApplication.currentUser.bio = binding.bioInput.text.toString()
                changeName(SettingsType.BIO, mApplication.currentUser.bio)
                changingsCount++
            }
            if (changingsCount > 0) replaceFragment(SettingsFragment())
            else showToast("No changes")

        }

        binding.backBtnFromDetail.setOnClickListener {
            appActivity.supportFragmentManager.popBackStack()
        }
    }

    private fun changeName(fieldType: SettingsType, input: String) {

        if (input == "") {
            showToast("Plese fill all fields")
        } else {
            if (isUsername == true) {
                mApplication.databaseFbRef.child(NODE_USERNAMES)
                    .addListenerForSingleValueEvent(AppValueEventListener {
                        if (it.hasChild(input.toString())) {
                            showToast("Already exist")
                        } else changeUserName(fieldType, input)
                    })
            } else {
                updateUserName(fieldType, input)
            }
        }
    }

    private fun changeUserName(fieldType: SettingsType, input: String) {
        mApplication.databaseFbRef.child(NODE_USERNAMES).child(input)
            .setValue(mApplication.currentUserID)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    updateUserName(fieldType, input)
                    deletePreUsername()
                }
            }
    }

    private fun deletePreUsername() {
        mApplication.databaseFbRef.child(NODE_USERNAMES).child(mApplication.currentUser.username)
            .removeValue()
            .addOnFailureListener { it.message.toString() }
    }

    private fun updateUserName(fieldType: SettingsType, input: String) {
        updateDbValue(input, fieldType.toString().lowercase())
    }
}