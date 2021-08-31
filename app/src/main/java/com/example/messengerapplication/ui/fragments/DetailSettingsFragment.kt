package com.example.messengerapplication.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.messengerapplication.R
import com.example.messengerapplication.databinding.FragmentDetailSettingsBinding
import com.example.messengerapplication.utilits.*

class DetailSettingsFragment(val fieldType: SettingsType) : Fragment(R.layout.fragment_detail_settings) {

    private lateinit var binding: FragmentDetailSettingsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetailSettingsBinding.bind(view)
    }

    private var result: String? = null
    private var isUsername: Boolean? = null

    override fun onStart() {
        super.onStart()

        binding.inputLabel
        binding.input
        binding.saveBtn

        isUsername = false
        when (fieldType) {
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
        }
        binding.backBtnFromDetail.setOnClickListener {
            APP_ACTIVITY.supportFragmentManager.popBackStack()
        }
    }

    private fun changeName() {

        if (result == "") {
            showToast("Заполните!")
        } else {
            if(isUsername==true){
                REF_DATABASE_ROOT.child(NODE_USERNAMES)
                    .addListenerForSingleValueEvent(AppValueEventListener {
                        if (it.hasChild(result.toString())) {
                            showToast("Уже существует!")
                        } else changeUserName()
                    })
            }else{
                updateUserName(fieldType)
                fragmentManager?.popBackStack()
                USER.fullname = result.toString()
            }
        }
    }

    private fun changeUserName() {
        REF_DATABASE_ROOT.child(NODE_USERNAMES).child(result.toString()).setValue(UID)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    updateUserName(fieldType)
                    deletePreUsername()
                }
            }
    }

    private fun deletePreUsername() {
        REF_DATABASE_ROOT.child(NODE_USERNAMES).child(USER.username).removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    fragmentManager?.popBackStack()
                    USER.username = result.toString()
                } else {
                    showToast(it.exception?.message.toString())
                }
            }
    }

    private fun updateUserName(fieldType: SettingsType) {
        updateField(result.toString(), fieldType.toString().lowercase())
    }
}