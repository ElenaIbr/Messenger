package com.example.messengerapplication.activities

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.example.messengerapplication.R
import com.example.messengerapplication.databinding.ActivityMainBinding
import com.example.messengerapplication.ui.fragments.chatlist.ChatFragment
import com.example.messengerapplication.ui.fragments.ContacstFragment
import com.example.messengerapplication.ui.fragments.authentication.EnterPhoneNumFragment
import com.example.messengerapplication.ui.fragments.profile.SettingsFragment
import com.example.messengerapplication.utilits.*
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        APP_ACTIVITY = this
    }

    override fun onResume() {
        super.onResume()

        initFirebase()

        initUser {
            initFunc()
            CoroutineScope(Dispatchers.IO).launch{
                initContacts()
            }

        }

        mBinding.bottomNav.setOnNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.contacts -> changeFragment(ContacstFragment(), false)
                R.id.messages -> changeFragment(ChatFragment(), false)
                R.id.profile -> changeFragment(SettingsFragment(), false)
            }
            true
        }

        AppStates.updateStates(AppStates.ONLINE)
    }

    private fun initFunc(){

        if(authFirebase.currentUser!=null){
            changeFragment(ChatFragment(), false)
        }else{
            mBinding.bottomNav.visibility = View.GONE
            changeFragment(EnterPhoneNumFragment(), false)
        }
    }

    override fun onStop() {
        super.onStop()
        AppStates.updateStates(AppStates.OFFLINE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(ContextCompat.checkSelfPermission(APP_ACTIVITY, READ_CONTACTS)== PackageManager.PERMISSION_GRANTED){
            initContacts()
        }
    }





}