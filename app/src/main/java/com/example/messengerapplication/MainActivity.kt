package com.example.messengerapplication

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.example.messengerapplication.databinding.ActivityMainBinding
import com.example.messengerapplication.ui.fragments.chatlist.ChatFragment
import com.example.messengerapplication.ui.fragments.ContactFragment
import com.example.messengerapplication.ui.fragments.authentication.EnterPhoneNumFragment
import com.example.messengerapplication.ui.fragments.SettingsFragment
import com.example.messengerapplication.utilits.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mBottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        APP_ACTIVITY = this


        initFirebase()
        initFunc()
        initUser {
            //initFunc()
            CoroutineScope(Dispatchers.IO).launch{
                initContacts()
            }

        }

        mBinding.bottomNav.setOnNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.contacts -> changeFragment(ContactFragment(), false)
                R.id.messages -> changeFragment(ChatFragment(), false)
                R.id.profile -> changeFragment(SettingsFragment(), false)
            }
            true
        }
    }

    private fun initFunc(){

        if(authFirebase.currentUser!=null){
            changeFragment(ChatFragment(), false)
        }else{
            //startOtherActivity(RegistrationActivity())
            mBinding.bottomNav.visibility = View.GONE
            changeFragment(EnterPhoneNumFragment(), false)
        }
    }

    override fun onStart() {
        super.onStart()
        AppStates.updateStates(AppStates.ONLINE)
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