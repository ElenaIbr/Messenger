package com.example.messengerapplication.utilits

import android.content.Context
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.messengerapplication.R
import com.example.messengerapplication.models.CommonModel
import com.example.messengerapplication.models.User
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

var messageCount : Int = 0

fun showToast(message: String){
    Toast.makeText(appActivity, message, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.restartActivity(activity: AppCompatActivity){
    val intent = Intent(this, activity::class.java)
    startActivity(intent)
    this.finish()
}

fun AppCompatActivity.changeFragment(fragment: Fragment, addStack: Boolean = true){
    if(addStack){
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.dataContainer, fragment)
            .commit()
    }
    else{
        supportFragmentManager.beginTransaction()
            .replace(R.id.dataContainer, fragment)
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
    val im: InputMethodManager = appActivity.getSystemService(Context.INPUT_METHOD_SERVICE)
            as InputMethodManager
    im.hideSoftInputFromWindow(appActivity.window.decorView.windowToken, 0)
}

fun ImageView.setImg(url: String = ""){
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
    FULLNAME("CHILD_FULLNAME"),
    BIO("CHILD_BIO")
}

fun EditText.phoneFormat(){
    val listener = MaskedTextChangedListener("+2 ([000]) [000]-[00]-[00]", this)

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

fun String.toDateFormat():String {

    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)

    if(Date(this.toLong()) > calendar.time){
        val time = Date(this.toLong())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(time)
    }else{
        val time = Date(this.toLong())
        val timeFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        return timeFormat.format(time)
    }
}

fun addBadge(count : Int) {
    val badge: BadgeDrawable = appActivity.findViewById<BottomNavigationView>(R.id.bottomNav)
        .getOrCreateBadge(R.id.messages)
    badge.number = count
    badge.isVisible = true
}

fun removeBadge(){
    appActivity.findViewById<BottomNavigationView>(R.id.bottomNav).removeBadge(R.id.messages)
}


