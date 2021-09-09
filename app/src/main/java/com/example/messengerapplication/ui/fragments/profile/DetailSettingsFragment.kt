package com.example.messengerapplication.ui.fragments.profile

import com.example.messengerapplication.databinding.FragmentDetailSettingsBinding
import com.example.messengerapplication.ui.fragments.BaseFragment
import com.example.messengerapplication.utilits.*

class DetailSettingsFragment() : BaseFragment<FragmentDetailSettingsBinding>() {

    private var isUsername: Boolean? = null

    override fun getViewBinding() = FragmentDetailSettingsBinding.inflate(layoutInflater)

    override fun onStart() {
        super.onStart()

        binding.usernameInput.setText(USER.username)
        binding.fullnameInput.setText(USER.fullname)
        binding.bioInput.setText(USER.bio)
        binding.saveSettingsBtn

        isUsername = false

        binding.saveSettingsBtn.setOnClickListener {
            var changingsCount: Byte = 0
            if(binding.usernameInput.text.toString() != USER.username){
                USER.username = binding.usernameInput.text.toString()
                isUsername = true
                changeName(SettingsType.USERNAME, USER.username)
                changingsCount++
            }
            if(binding.fullnameInput.text.toString() != USER.fullname){
                USER.fullname = binding.fullnameInput.text.toString()
                changeName(SettingsType.FULLNAME, USER.fullname)
                changingsCount++
            }
            if(binding.bioInput.text.toString() != USER.bio){
                USER.bio = binding.bioInput.text.toString()
                changeName(SettingsType.BIO, USER.bio)
                changingsCount++
            }
            if(changingsCount>0) replaceFragment(SettingsFragment())
            else showToast("Нет изменений")

        }

        binding.backBtnFromDetail.setOnClickListener {
            APP_ACTIVITY.supportFragmentManager.popBackStack()
        }
    }

    private fun changeName(fieldType:SettingsType, input: String) {

        if (input == "") {
            showToast("Заполните!")
        } else {
            if(isUsername==true){
                REF_DATABASE_ROOT.child(NODE_USERNAMES)
                    .addListenerForSingleValueEvent(AppValueEventListener {
                        if (it.hasChild(input.toString())) {
                            showToast("Уже существует!")
                        } else changeUserName(fieldType, input)
                    })
            }else{
                updateUserName(fieldType, input)
            }
        }
    }

    private fun changeUserName(fieldType: SettingsType, input: String) {
        REF_DATABASE_ROOT.child(NODE_USERNAMES).child(input).setValue(UID)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    updateUserName(fieldType, input)
                    deletePreUsername()
                }
            }
    }

    private fun deletePreUsername() {
        REF_DATABASE_ROOT.child(NODE_USERNAMES).child(USER.username).removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {

                } else {
                    showToast(it.exception?.message.toString())
                }
            }
    }

    private fun updateUserName(fieldType: SettingsType, input: String) {
        updateField(input.toString(), fieldType.toString().lowercase())
    }
}