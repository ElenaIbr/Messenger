package com.example.messengerapplication.ui.fragments.authentication

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import com.example.messengerapplication.R
import com.example.messengerapplication.MainActivity
import com.example.messengerapplication.databinding.FragmentEnterCodeBinding
import com.example.messengerapplication.notifications.FirebaseService
import com.example.messengerapplication.ui.fragments.BaseFragment
import com.example.messengerapplication.utilits.*
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.iid.FirebaseInstanceId

@Suppress("DEPRECATION")
class EnterCodeFragment(private val phoneNum: String, val id: String) :
    BaseFragment<FragmentEnterCodeBinding>() {

    private var pressed: Boolean = false

    override fun getViewBinding() = FragmentEnterCodeBinding.inflate(layoutInflater)

    override fun onStart() {

        super.onStart()
        binding.regCodeInput.requestFocus()
        countdown()

        binding.regCodeInput.addTextChangedListener(AppTextWatcher {
            val code = binding.regCodeInput.text.toString()
            if (code.length == 6) {
                pressed = true
                enterCode()
            }
        })
    }

    private fun enterCode() {
        val credential = PhoneAuthProvider.getCredential(id, binding.regCodeInput.text.toString())
        mApplication.authFb.signInWithCredential(credential)
            .addOnFailureListener { showToast(it.message.toString()) }
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    //here we create node for new user (uid, phone number, token)
                    val uid = mApplication.authFb.currentUser?.uid.toString()
                    val dateMap = mutableMapOf<String, Any>()
                    dateMap[CHILD_ID] = uid
                    dateMap[CHILD_PHONE] = phoneNum

                    //get device token
                    FirebaseService.sharedPref =
                        appActivity.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
                    FirebaseInstanceId.getInstance().instanceId
                        .addOnSuccessListener { instance ->
                            FirebaseService.token = instance.token
                            dateMap[CHILD_TOKEN] = instance.token
                        }
                        .addOnFailureListener { error -> showToast(error.message.toString()) }


                    mApplication.databaseFbRef.child(NODE_PHONES).child(phoneNum).setValue(uid)
                        .addOnFailureListener { error -> showToast(error.message.toString()) }
                        .addOnSuccessListener {
                            mApplication.databaseFbRef.child(NODE_USERS).child(uid)
                                .updateChildren(dateMap)
                                .addOnCompleteListener {
                                    appActivity.restartActivity(MainActivity())
                                    showToast(getString(R.string.welcome))
                                }
                                .addOnFailureListener { error -> showToast(error.message.toString()) }
                        }
                } else showToast(it.exception?.message.toString())
            }
    }

    //timer for code verification
    private fun countdown() {
        object : CountDownTimer(59000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                if (this@EnterCodeFragment.pressed) {
                    cancel()
                }
                binding.chronometer.text = "00:${(millisUntilFinished / 1000)}"
            }
            override fun onFinish() {
                appActivity.restartActivity(MainActivity())
            }
        }.start()
    }
}