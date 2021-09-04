package com.example.messengerapplication.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.example.messengerapplication.R
import com.example.messengerapplication.databinding.ActivityRegistrationBinding
import com.example.messengerapplication.ui.fragments.authentication.EnterPhoneNumFragment
import com.example.messengerapplication.utilits.changeFragment
import com.example.messengerapplication.utilits.initFirebase

class RegistrationActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityRegistrationBinding
    private lateinit var mToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initFirebase()

        changeFragment(EnterPhoneNumFragment(), false)
    }

    override fun onStart() {
        super.onStart()
        //mToolbar = mBinding.regToolbar
        //setSupportActionBar(mToolbar)
        //title = getString(R.string.reg_your_phone)
    }
}