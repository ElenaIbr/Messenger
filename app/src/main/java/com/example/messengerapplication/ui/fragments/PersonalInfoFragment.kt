package com.example.messengerapplication.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import com.example.messengerapplication.R
import com.example.messengerapplication.databinding.FragmentPersonalInfoBinding
import com.example.messengerapplication.models.CommonModel

class PersonalInfoFragment(val contact: CommonModel) : Fragment(R.layout.fragment_personal_info) {

    private lateinit var binding: FragmentPersonalInfoBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPersonalInfoBinding.bind(view)
    }

    override fun onStart() {
        super.onStart()
        binding.contactPhone.text = contact.phone
        Log.d("MyLog", "dfdfdf ${contact.phone}")
    }

    override fun onResume() {
        super.onResume()
        updateInfo()
    }

    private fun updateInfo() {
        binding.contactPhone.text = contact.phone
        Log.d("MyLog", "${contact.phone}")
        binding.contactUsername.text = contact.username
        binding.contactBio.text = contact.boi
    }

}