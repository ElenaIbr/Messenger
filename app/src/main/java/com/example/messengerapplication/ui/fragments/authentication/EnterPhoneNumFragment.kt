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

    private lateinit var callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onStart() {
        super.onStart()
        binding.regNextBtn.setOnClickListener { sendCode() }
    }

    private fun sendCode() {
        if (binding.regCodeNum.text.toString().isEmpty()) {
            showToast(getString(R.string.enter_phone))
        } else authUser()
    }

    private fun authUser() {
        verifyCallback()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            binding.regCodeNum.text.toString(),
            60,
            TimeUnit.SECONDS,
            appActivity,
            callback
        )
    }

    private fun verifyCallback() {
        callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                mApplication.authFb
                    .signInWithCredential(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            showToast(getString(R.string.welcome))
                            AppStates.updateStates(AppStates.ONLINE)
                            appActivity.restartActivity(MainActivity())
                        } else showToast(task.exception?.message.toString())
                    }
            }

            override fun onCodeSent(
                verificationId: String,
                p1: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, p1)
                appActivity.changeFragment(
                    EnterCodeFragment(
                        binding.regCodeNum.text.toString(),
                        verificationId
                    )
                )
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                showToast(p0.message.toString())
            }
        }
    }
}