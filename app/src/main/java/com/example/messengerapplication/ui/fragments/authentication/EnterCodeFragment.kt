package com.example.messengerapplication.ui.fragments.authentication

import android.content.Context
import android.os.CountDownTimer
import com.example.messengerapplication.R
import com.example.messengerapplication.activities.MainActivity
import com.example.messengerapplication.databinding.FragmentEnterCodeBinding
import com.example.messengerapplication.notifications.FirebaseService
import com.example.messengerapplication.ui.fragments.BaseFragment
import com.example.messengerapplication.utilits.*
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.iid.FirebaseInstanceId

class EnterCodeFragment(val phoneNum: String, val id: String) : BaseFragment<FragmentEnterCodeBinding>() {

    private var pressed: Boolean = false

    override fun getViewBinding() = FragmentEnterCodeBinding.inflate(layoutInflater)

    override fun onStart() {

        super.onStart()
        binding.regCodeInput.requestFocus()
        countdown()

        binding.regCodeInput.addTextChangedListener(AppTextWatcher{
            val code = binding.regCodeInput.text.toString()
            if(code.length==6){
                pressed = true
                enterCode()
            }
        })
    }

    private fun enterCode(){
        val credential = PhoneAuthProvider.getCredential(id, binding.regCodeInput.text.toString())
        authFirebase.signInWithCredential(credential)
            .addOnFailureListener {showToast(it.message.toString())}
            .addOnCompleteListener {
            if(it.isSuccessful){
                val uid = authFirebase.currentUser?.uid.toString()
                val dateMap = mutableMapOf<String, Any>()
                dateMap[CHILD_ID] = uid
                dateMap[CHILD_PHONE] = phoneNum

                FirebaseService.sharedPref = APP_ACTIVITY.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
                FirebaseInstanceId.getInstance().instanceId
                    .addOnSuccessListener {
                        FirebaseService.token = it.token
                        dateMap[CHILD_TOKEN] = it.token
                    }
                    .addOnFailureListener { showToast(it.message.toString()) }


                REF_DATABASE_ROOT.child(NODE_PHONES).child(phoneNum).setValue(uid)
                    .addOnFailureListener { showToast(it.message.toString()) }
                    .addOnSuccessListener {
                        REF_DATABASE_ROOT.child(NODE_USERS).child(uid).updateChildren(dateMap)
                            .addOnCompleteListener {
                                showToast(getString(R.string.welcome))
                                APP_ACTIVITY.restartActivity(MainActivity())
                            }
                            .addOnFailureListener { showToast(it.message.toString()) }
                    }
            }
            else showToast(it.exception?.message.toString())
        }
    }

    private fun countdown() {
        object : CountDownTimer(59000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if(this@EnterCodeFragment.pressed){
                    cancel()
                }
                binding.chronometer.text = "00:${(millisUntilFinished / 1000)}"
            }
            override fun onFinish() {
                APP_ACTIVITY.restartActivity(MainActivity())
            }
        }.start()
    }
}