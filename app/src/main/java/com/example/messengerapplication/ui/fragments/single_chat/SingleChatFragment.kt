package com.example.messengerapplication.ui.fragments.single_chat

import android.content.Context
import android.util.Log
import android.view.MenuInflater
import android.view.View
import android.widget.AbsListView
import android.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapplication.R
import com.example.messengerapplication.databinding.FragmentSingleChatBinding
import com.example.messengerapplication.models.CommonModel
import com.example.messengerapplication.models.User
import com.example.messengerapplication.notifications.FirebaseService
import com.example.messengerapplication.notifications.NotificationData
import com.example.messengerapplication.notifications.PushNotification
import com.example.messengerapplication.notifications.RetrofitInstance
import com.example.messengerapplication.ui.fragments.BaseFragment
import com.example.messengerapplication.ui.fragments.profile.SettingsFragment
import com.example.messengerapplication.ui.fragments.chatlist.ChatFragment
import com.example.messengerapplication.utilits.*
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.Ref


class SingleChatFragment(val contact: CommonModel) : BaseFragment<FragmentSingleChatBinding>() {

    private var act: View? = null
    private lateinit var headInfoListener: AppValueEventListener
    private lateinit var receivingUser: User
    private lateinit var userRef: DatabaseReference

    private lateinit var messagesRef: DatabaseReference
    private lateinit var adapter: SingleChatAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageListener: AppChildrenEventListener
    private lateinit var mLayoutManager: LinearLayoutManager

    private var mCountMessenger: Int = 10
    private var mIsScrolling: Boolean = false
    private var mSmoothScrollToPosition = true

    override fun getViewBinding() = FragmentSingleChatBinding.inflate(layoutInflater)


    override fun onResume() {
        super.onResume()

        REF_DATABASE_ROOT.child(NODE_CHATLIST).child(UID).child(contact.id).child(
            CHILD_MESSAGE_COUNT).setValue(0.0)
        Log.d("MyLog", "мы тут")

        act = activity?.findViewById(R.id.bottomNav)
        act?.visibility = View.GONE

        mLayoutManager = LinearLayoutManager(this.context)
        mLayoutManager.stackFromEnd = true

        headInfoListener = AppValueEventListener {
            receivingUser = it.getUser()
            updateToolbarInfo()
        }

        userRef = REF_DATABASE_ROOT.child(NODE_USERS).child(contact.id)
        userRef.addValueEventListener(headInfoListener)
        binding.backBtn.setOnClickListener {
            APP_ACTIVITY.supportFragmentManager.popBackStack()
        }

        binding.sendMessageBtn.setOnClickListener {
            initHeaderInfo()
        }

        binding.toolbarImg.setOnClickListener {
            //replaceFragment(PersonalInfoFragment(contact))
            replaceFragment(
                SettingsFragment(contact.username,
                contact.fullname,
                contact.phone,
                contact.bio,
                contact.photoUrl,
                true)
            )

        }

        initRecyclerView()

        binding.menuBtn.setOnClickListener {
            showPopup(binding.menuBtn)
        }
    }

    private fun initRecyclerView() {

        recyclerView = binding.rcMessage
        adapter = SingleChatAdapter()
        messagesRef = REF_DATABASE_ROOT.child(NODE_MESSAGES)
            .child(UID)
            .child(contact.id)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = mLayoutManager

        messageListener = AppChildrenEventListener{
            val message = it.getCommonModel()
            if(mSmoothScrollToPosition){
                adapter.addItemToBottom(message){
                    recyclerView.smoothScrollToPosition(adapter.itemCount)
                }
            }else{
                adapter.addItemToTop(message){
                    binding.chatSwipe.isRefreshing = false
                }
            }
        }
        messagesRef.removeEventListener(messageListener)
        messagesRef.limitToLast(mCountMessenger).addChildEventListener(messageListener)


        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(mIsScrolling && dy < 0 && mLayoutManager.findFirstVisibleItemPosition()<=3){
                    updateData()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    mIsScrolling = true
                }
            }
        })

        binding.chatSwipe.setOnRefreshListener { updateData() }
    }

    private fun updateData() {
        mSmoothScrollToPosition = false
        mIsScrolling = false
        mCountMessenger += 10
        messagesRef.removeEventListener(messageListener)
        messagesRef.limitToLast(mCountMessenger).addChildEventListener(messageListener)
    }

    private fun initHeaderInfo() {
        mSmoothScrollToPosition = true
        val message = binding.chatInputMessage.text.toString()
        if (message.isEmpty()) {
            showToast("Введите текст!")
        } else sentMessage(message, contact.id, TYPE_TEXT) {
            binding.chatInputMessage.setText("")
            Log.d("MyLog", "${messgeCount}")
            saveToChatlist(contact.id, contact.namefromcontacts, TYPE_TEXT)

        }
    }

    private fun sentMessage(message: String,
                            receivingUserID: String,
                            typeText: String,
                            function: () -> Unit) {
        val refDialogUser = "$NODE_MESSAGES/$UID/$receivingUserID"
        val refReceivDialogUser = "$NODE_MESSAGES/$receivingUserID/$UID"

        val messageKey = REF_DATABASE_ROOT.child(refDialogUser).push().key

        val mapMessage = hashMapOf<String, Any>()
        mapMessage[CHILD_FROM] = UID
        mapMessage[CHILD_TYPE] = typeText
        mapMessage[CHILD_TEXT] = message
        mapMessage[CHILD_ID] = messageKey.toString()
        mapMessage[CHILD_TIMESTAMP] = ServerValue.TIMESTAMP
        mapMessage[CHILD_MESSAGE_STATUS] = "не прочитано"

        val mapDialod = hashMapOf<String, Any>()
        mapDialod["$refDialogUser/$messageKey"] = mapMessage
        mapDialod["$refReceivDialogUser/$messageKey"] = mapMessage

        REF_DATABASE_ROOT
            .updateChildren(mapDialod)
            .addOnSuccessListener { function() }
            .addOnFailureListener { showToast(it.message.toString()) }

        FirebaseService.sharedPref = APP_ACTIVITY.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId
            .addOnFailureListener { Log.d("MyLog", "токен ${it.message.toString()}") }
            .addOnSuccessListener {
                FirebaseService.token = it.token
                val token = it.token
                Log.d("MyLog", "токен ${it.token}")
            }

        val title = "От"
        if(title.isNotEmpty() && message.isNotEmpty() && receivingUserID.isNotEmpty()) {
            PushNotification(
                NotificationData("from: ${phoneFormat(USER.phone)}", message),
                contact.token
            ).also {
                sendNotification(it)
            }
        }
    }

    private fun updateToolbarInfo() {
        binding.toolbarImg.setImg(receivingUser.photoUrl)
        binding.toolbarContactName.text = contact.namefromcontacts
        binding.toolbarContactStatus.text = receivingUser.state
    }

    override fun onPause() {
        super.onPause()
        act?.visibility = View.VISIBLE
        userRef.removeEventListener(headInfoListener)
        messagesRef.removeEventListener(messageListener)
    }

    fun showPopup(v : View){
        val popup = PopupMenu(APP_ACTIVITY, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.single_chat_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.clear_chat-> {
                    clearChat(contact.id){
                        showToast("Чат очищен")
                        replaceFragment(ChatFragment())
                    }
                }
                R.id.delete_chat-> {
                    deleteChat(contact.id){
                        showToast("Чат удален")
                        replaceFragment(ChatFragment())
                    }
                }
            }
            true
        }
        popup.show()
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                //Log.d(ContentValues.TAG, "Response: ${Gson().toJson(response)}")
            } else {
                //Log.e(ContentValues.TAG, response.errorBody().toString())
            }
        } catch(e: Exception) {
            //Log.e(ContentValues.TAG, e.toString())
        }
    }
}