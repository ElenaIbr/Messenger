package com.example.messengerapplication.ui.fragments.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import com.example.messengerapplication.activities.MainActivity
import com.example.messengerapplication.R
import com.example.messengerapplication.databinding.FragmentSettingsBinding
import com.example.messengerapplication.ui.fragments.BaseFragment
import com.example.messengerapplication.utilits.*
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

class SettingsFragment(val userName: String = USER.username,
                       val fullName: String = USER.fullname,
                       val num: String = USER.phone,
                       val bio: String = USER.bio,
                       val photo: String = USER.photoUrl,
                       val isOtherUser: Boolean = false) : BaseFragment<FragmentSettingsBinding>() {

    override fun getViewBinding() = FragmentSettingsBinding.inflate(layoutInflater)

    var name: String? = null

    override fun onResume() {
        hideKeyboard()
        super.onResume()
        setHasOptionsMenu(true)

        binding.changenumberTv.text = phoneFormat(num)


        checkInfo(fullName, binding.changeInfoTv)
        checkInfo(userName, binding.usernameContent)
        checkInfo(bio, binding.changeBioTv)

        binding.settingsPhoto.setImg(photo)

        if(!isOtherUser){
            binding.menuSettings.setOnClickListener {
                showPopup(binding.menuSettings)
            }
        }
        else {
            binding.menuSettings.visibility = View.GONE
            binding.changePhoto.visibility = View.GONE
            binding.backToChat.visibility = View.VISIBLE

            binding.backToChat.setOnClickListener {
                APP_ACTIVITY.supportFragmentManager.popBackStack()
            }
        }

        binding.changePhoto.setOnClickListener {
            changeUserPhoto()
        }

    }

    private fun checkInfo(info: String, textView: TextView, text: String = "no info") {
        if (!info.isEmpty()) {
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
            .start(APP_ACTIVITY, this)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.menuInflater?.inflate(R.menu.single_chat_menu, menu)
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
                        USER.photoUrl = it
                        APP_ACTIVITY.changeFragment(SettingsFragment())
                        showToast("Updated!")
                        USER.photoUrl = it
                    }
                }
            }
        }}

    fun showPopup(v : View){
        val popup = PopupMenu(APP_ACTIVITY, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.settings_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.sign_out-> {
                    authFirebase.signOut()
                    AppStates.updateStates(AppStates.OFFLINE)
                    APP_ACTIVITY.restartActivity(MainActivity())
                }
                R.id.edit_photo-> {
                    //changeUserPhoto()
                    REF_STORAGE_ROOT
                        .child(FOLDER_PROFILE_IMG)
                        .child(UID)
                        .delete()
                    REF_DATABASE_ROOT.child(NODE_USERS)
                        .child(UID)
                        .child(CHILD_PHOTO_URL)
                        .removeValue()
                    binding.settingsPhoto.setImg()
                    USER.photoUrl = ""
                }
                R.id.edit_info-> {
                    replaceFragment(DetailSettingsFragment())
                }
            }
            true
        }
        popup.show()
    }
}