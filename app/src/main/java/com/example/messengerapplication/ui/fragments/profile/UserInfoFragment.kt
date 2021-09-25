package com.example.messengerapplication.ui.fragments.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import com.example.messengerapplication.R
import com.example.messengerapplication.activities.MainActivity
import com.example.messengerapplication.databinding.FragmentSettingsBinding
import com.example.messengerapplication.models.CommonModel
import com.example.messengerapplication.ui.fragments.BaseFragment
import com.example.messengerapplication.utilits.*
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

class UserInfoFragment(
    private val user: CommonModel = CommonModel(),
    private val isOtherUser: Boolean = false
) : BaseFragment<FragmentSettingsBinding>() {

    override fun getViewBinding() = FragmentSettingsBinding.inflate(layoutInflater)

    var name: String? = null

    override fun onResume() {
        hideKeyboard()
        super.onResume()
        setHasOptionsMenu(true)

        if (!isOtherUser) {
            binding.changenumberTv.text = mApplication.currentUser.phone
            checkInfo(mApplication.currentUser.fullname, binding.changeInfoTv)
            checkInfo(mApplication.currentUser.username, binding.usernameContent)
            checkInfo(mApplication.currentUser.bio, binding.changeBioTv)
            binding.settingsPhoto.setImg(mApplication.currentUser.photoUrl)

            binding.menuSettings.setOnClickListener {
                showPopup(binding.menuSettings)
            }

        } else {
            binding.menuSettings.visibility = View.GONE
            binding.changePhoto.visibility = View.GONE
            binding.backToChat.visibility = View.VISIBLE

            binding.changenumberTv.text = user.phone
            checkInfo(user.fullname, binding.changeInfoTv)
            checkInfo(user.username, binding.usernameContent)
            checkInfo(user.bio, binding.changeBioTv)
            binding.settingsPhoto.setImg(user.photoUrl)

            binding.backToChat.setOnClickListener {
                appActivity.supportFragmentManager.popBackStack()
            }
        }
        binding.changePhoto.setOnClickListener {
            changeUserPhoto()
        }
    }

    private fun checkInfo(info: String, textView: TextView, text: String = "no info") {
        if (info.isNotEmpty()) {
            textView.text = info
        } else {
            textView.setTextColor(Color.GRAY)
            textView.text = text
        }
    }

    private fun changeUserPhoto() {
        CropImage.activity()
            .setAspectRatio(1, 1)
            .setRequestedSize(600, 600)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(appActivity, this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.menuInflater?.inflate(R.menu.single_chat_menu, menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
            && resultCode == Activity.RESULT_OK && data != null
        ) {
            val uri = CropImage.getActivityResult(data).uri
            val path = mApplication.storageFbRef.child(FOLDER_PROFILE_IMG)
                .child(mApplication.currentUserID)

            putImageToStorage(uri, path) {
                getUrlFromStorage(path) {
                    putUrlToDb(it) {
                        //binding.settingsPhoto.setImg(it)
                        appActivity.changeFragment(UserInfoFragment())
                        showToast("Данные обновлены")
                        mApplication.currentUser.photoUrl = it
                    }
                }
            }
        }
    }

    private fun showPopup(v: View) {
        val popup = PopupMenu(appActivity, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.settings_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sign_out -> {
                    AppStates.updateStates(AppStates.OFFLINE)
                    mApplication.authFb.signOut()
                    appActivity.restartActivity(MainActivity())
                }
                R.id.edit_photo -> {
                    mApplication.storageFbRef
                        .child(FOLDER_PROFILE_IMG)
                        .child(mApplication.currentUserID)
                        .delete()
                    mApplication.databaseFbRef.child(NODE_USERS)
                        .child(mApplication.currentUserID)
                        .child(CHILD_PHOTO_URL)
                        .removeValue()
                    binding.settingsPhoto.setImg()
                    mApplication.currentUser.photoUrl = ""
                }
                R.id.edit_info -> {
                    replaceFragment(EditUserInfoFragment())
                }
            }
            true
        }
        popup.show()
    }
}