package com.example.messengerapplication.utilits

import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.messengerapplication.MainActivity
import com.example.messengerapplication.R
import com.example.messengerapplication.databinding.ActivityMainBinding
import com.example.messengerapplication.models.CommonModel
import com.example.messengerapplication.models.User
import com.google.firebase.database.DataSnapshot
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

fun showToast(message: String){
    Toast.makeText(APP_ACTIVITY, message, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.startOtherActivity(activity: AppCompatActivity){
    val intent = Intent(this, activity::class.java)
    startActivity(intent)
    this.finish()
}

fun AppCompatActivity.changeFragment(container: Int, fragment: Fragment, addStack: Boolean = true){
    if(addStack){
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(container, fragment)
            .commit()
    }
    else{
        supportFragmentManager.beginTransaction()
            .replace(container, fragment)
            .commit()
    }
}

fun Fragment.replaceFragment(fragment: Fragment, addStack: Boolean = true){
    if(addStack){
        fragmentManager?.beginTransaction()
            ?.replace(R.id.dataContainer, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }
    else{
        fragmentManager?.beginTransaction()
            ?.replace(R.id.dataContainer, fragment)
            ?.commit()
    }
}

fun hideKeyboard() {
    val im: InputMethodManager = APP_ACTIVITY.getSystemService(Context.INPUT_METHOD_SERVICE)
            as InputMethodManager
    im.hideSoftInputFromWindow(APP_ACTIVITY.window.decorView.windowToken, 0)
}

fun ImageView.setImg(url: String){
    if(url==""){
        this.setImageResource(R.drawable.ic_default_user)
    }else{
        Picasso.get()
            .load(url)
            .placeholder(R.drawable.ic_default_user)
            .into(this)
    }
}

fun phoneFormat(number: String, region: String = "RU"): String{
    val pnu = PhoneNumberUtil.getInstance()
    val pn = pnu.parse(number, region)
    val pnE164 = pnu.format(pn, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
    return pnE164
}

enum class SettingsType(val state: String) {
    PHONE("CHILD_PHONE"),
    USERNAME("CHILD_USERNAME"),
    FULLNAME("CHILD_FULLNAME")
}

fun EditText.phoneFormat(){
    val listener = MaskedTextChangedListener("+7 ([000]) [000]-[00]-[00]", this)

    this.addTextChangedListener(listener)
    this.onFocusChangeListener = listener
}

fun DataSnapshot.getUser() : User = this.getValue(User::class.java) ?: User()
fun DataSnapshot.getCommonModel() : CommonModel = this.getValue(CommonModel::class.java) ?: CommonModel()

fun String.toTimeFormat():String {
    val time = Date(this.toLong())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return timeFormat.format(time)
}
