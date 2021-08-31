package com.example.messengerapplication.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import com.example.messenger.ui.fragments.BaseFragment
import com.example.messengerapplication.MainActivity
import com.example.messengerapplication.R
import com.example.messengerapplication.activities.RegistrationActivity
import com.example.messengerapplication.databinding.FragmentSettingsBinding
import com.example.messengerapplication.utilits.*
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

class SettingsFragment : BaseFragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
    }

    var name: String? = null
    var fullName: String? = null

    override fun onStart() {
        super.onStart()
        //APP_ACTIVITY.mDrawer.disableDrawer()
        hideKeyboard()

        binding.changeInfoTv.text = USER.fullname
        binding.changenumberTv.text = USER.phone
        binding.changenameTv.text = USER.username
        binding.userName.text = USER.fullname
        binding.settingsPhoto.setImg(USER.photoUrl)

        binding.settingsPhoto.setOnClickListener() {
            changeUserPhoto()
        }

        binding.btnChangeNum.setOnClickListener {
            replaceFragment(DetailSettingsFragment(SettingsType.PHONE))
        }
        binding.btnChangeUsername.setOnClickListener {
            replaceFragment(DetailSettingsFragment(SettingsType.USERNAME))
        }
        binding.btnChangeInfo.setOnClickListener {
            replaceFragment(DetailSettingsFragment(SettingsType.FULLNAME))
        }

        binding.signOutBtn.setOnClickListener {
            authFirebase.signOut()
            AppStates.updateStates(AppStates.OFFLINE)
            APP_ACTIVITY.startOtherActivity(MainActivity())
        }
    }

    private fun changeUserPhoto() {
        CropImage.activity()
            .setAspectRatio(1, 1)
            .setRequestedSize(600, 600)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(APP_ACTIVITY, this)
    }
    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
        //APP_ACTIVITY.title = getString(R.string.account)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.menuInflater?.inflate(R.menu.settings_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.setting_menu_exit -> {
                authFirebase.signOut()
                AppStates.updateStates(AppStates.OFFLINE)
                APP_ACTIVITY.startOtherActivity(MainActivity())
            }
        }
        return true
    }

    override fun onStop() {
        super.onStop()
        //APP_ACTIVITY.mDrawer.enableDrawer()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
            && resultCode == Activity.RESULT_OK && data!=null) {
            val uri = CropImage.getActivityResult(data).uri
            val path = REF_STORAGE_ROOT.child(FOLDER_PROFILE_IMG)
                .child(UID)

            putImageToStorage(uri, path){
                getUrlFromStorage(path){
                    putUrlToDatabase(it){
                        binding.settingsPhoto.setImg(it)
                        showToast("Данные обновлены")
                        USER.photoUrl = it
                        //APP_ACTIVITY.mDrawer.updateHeader()
                    }
                }
            }
        }}
}