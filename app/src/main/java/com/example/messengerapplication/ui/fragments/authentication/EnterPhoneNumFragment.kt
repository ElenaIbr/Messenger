package com.example.messengerapplication.ui.fragments.authentication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.example.messengerapplication.activities.MainActivity
import com.example.messengerapplication.R
import com.example.messengerapplication.databinding.FragmentEnterPhoneNumBinding
import com.example.messengerapplication.utilits.*
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class EnterPhoneNumFragment : Fragment(R.layout.fragment_enter_phone_num) {
    private var regBtnNext: Button? = null
    private var regInput: EditText? = null

    private lateinit var binding: FragmentEnterPhoneNumBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEnterPhoneNumBinding.bind(view)
    }


    private lateinit var phoneNum: String
    private lateinit var callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onStart() {
        super.onStart()

        regBtnNext = binding.regNextBtn
        regInput = binding.regCodeNum
        //regInput?.requestFocus()
        //regInput?.phoneFormat()


        regBtnNext?.setOnClickListener { sendCode() }
    }

    private fun sendCode(){
        if(regInput?.text.toString().isEmpty()){
            showToast("Введите номер!")
        }else authUser()

    }

    private fun authUser(){
        phoneNum = regInput?.text.toString()
        verifyCallback()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNum,
            60,
            TimeUnit.SECONDS,
            APP_ACTIVITY,
            callback
        )
    }

    private fun verifyCallback(){
        callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                authFirebase.signInWithCredential(credential).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        showToast("Добро пожаловать!")
                        APP_ACTIVITY.startOtherActivity(MainActivity())
                    }
                    else{
                        showToast(task.exception?.message.toString())
                    }
                }
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                replaceFragment(EnterCodeFragment(phoneNum, p0), false)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                //showToast(p0.message.toString())
                Log.d("MyLog", "${p0.message.toString()}")
            }
        }
    }
}