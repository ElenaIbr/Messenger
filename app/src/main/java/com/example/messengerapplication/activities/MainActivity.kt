package com.example.messengerapplication.activities

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.messengerapplication.R
import com.example.messengerapplication.app.MyApplication
import com.example.messengerapplication.databinding.ActivityMainBinding
import com.example.messengerapplication.models.User
import com.example.messengerapplication.ui.fragments.ContacstFragment
import com.example.messengerapplication.ui.fragments.authentication.EnterPhoneNumFragment
import com.example.messengerapplication.ui.fragments.chatlist.ChatFragment
import com.example.messengerapplication.ui.fragments.profile.SettingsFragment
import com.example.messengerapplication.utilits.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        appActivity = this
        mApplication = (this.application as MyApplication)
    }

    override fun onResume() {
        super.onResume()
        checkAuth()
    }

    override fun onPause() {
        super.onPause()
        AppStates.updateStates(AppStates.OFFLINE)
    }

    private fun checkAuth() {

        if (mApplication.authFb.currentUser != null) {

            mApplication.currentUser = User()
            mApplication.currentUserID = mApplication.authFb.currentUser?.uid.toString()

            initCurrentUser {
                CoroutineScope(Dispatchers.IO).launch {
                    changeFragment(ChatFragment(), false)
                    initUserContacts()
                }
            }
            //set bottom navigation view
            mBinding.bottomNav.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.contacts -> changeFragment(ContacstFragment(), false)
                    R.id.messages -> changeFragment(ChatFragment(), false)
                    R.id.profile -> changeFragment(SettingsFragment(), false)
                }
                true
            }
            AppStates.updateStates(AppStates.ONLINE)

        } else {
            mBinding.bottomNav.visibility = View.GONE
            changeFragment(EnterPhoneNumFragment(), false)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //chek permissions for reading users contacts
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (ContextCompat.checkSelfPermission(
                appActivity,
                READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            initUserContacts()
        }
    }
}

