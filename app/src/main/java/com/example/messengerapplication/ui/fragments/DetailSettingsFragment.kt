package com.example.messengerapplication.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.messengerapplication.R
import com.example.messengerapplication.databinding.FragmentDetailSettingsBinding
import com.example.messengerapplication.utilits.*

class DetailSettingsFragment() : Fragment(R.layout.fragment_detail_settings) {

    private lateinit var binding: FragmentDetailSettingsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetailSettingsBinding.bind(view)
    }

    private var result: String? = null
    private var isUsername: Boolean? = null
    private var field: SettingsType? = null

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

        /*when (fieldType) {
            SettingsType.PHONE -> {
                binding.inputLabel.text = "Номер телефона:"
                binding.input.setText(USER.phone)
            }
            SettingsType.USERNAME-> {
                binding.inputLabel.text = "Имя пользователя:"
                binding.input.setText(USER.username)
                isUsername = true
            }
            SettingsType.FULLNAME -> {
                binding.inputLabel.text = "Имя и фамилия:"
                binding.input.setText(USER.fullname)
            }

        }
        binding.saveBtn.setOnClickListener {
            result = binding.input.text.toString()
            changeName()
        }*/
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
                //fragmentManager?.popBackStack()
                //USER.fullname = result.toString()
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
                    //fragmentManager?.popBackStack()
                    //USER.username = result.toString()
                } else {
                    showToast(it.exception?.message.toString())
                }
            }
    }

    private fun updateUserName(fieldType: SettingsType, input: String) {
        updateField(input.toString(), fieldType.toString().lowercase())
    }
}