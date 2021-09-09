package com.example.messengerapplication.ui.fragments.authentication

import com.example.messengerapplication.R
import com.example.messengerapplication.activities.MainActivity
import com.example.messengerapplication.databinding.FragmentEnterPhoneNumBinding
import com.example.messengerapplication.ui.fragments.BaseFragment
import com.example.messengerapplication.utilits.*
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class EnterPhoneNumFragment : BaseFragment<FragmentEnterPhoneNumBinding>() {

    override fun getViewBinding() = FragmentEnterPhoneNumBinding.inflate(layoutInflater)

    private lateinit var phoneNum: String
    private lateinit var callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onStart() {
        super.onStart()
        binding.regNextBtn.setOnClickListener { sendCode() }
    }

    private fun sendCode(){
        if(binding.regCodeNum.text.toString().isEmpty()){
            showToast(getString(R.string.enter_phone))
        }else authUser()
    }

    private fun authUser(){
        phoneNum = binding.regCodeNum.text.toString()
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
                authFirebase
                    .signInWithCredential(credential).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        showToast(getString(R.string.welcome))
                        APP_ACTIVITY.restartActivity(MainActivity())
                    }
                    else showToast(task.exception?.message.toString())
                }
            }
            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                replaceFragment(EnterCodeFragment(phoneNum, p0), false)
            }
            override fun onVerificationFailed(p0: FirebaseException) {
                showToast(p0.message.toString())
            }
        }
    }
}