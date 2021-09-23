package com.example.messengerapplication.ui.fragments.single_chat

import android.content.Context
import android.view.MenuInflater
import android.view.View
import android.widget.AbsListView
import android.widget.PopupMenu
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
import com.example.messengerapplication.ui.fragments.chatlist.ChatFragment
import com.example.messengerapplication.ui.fragments.profile.SettingsFragment
import com.example.messengerapplication.utilits.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class SingleChatFragment(val contact: CommonModel, val chatFromContacts: String = "") :
    BaseFragment<FragmentSingleChatBinding>() {

    private var mBottomNavigation: View? = null
    private lateinit var mAdapter: SingleChatAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mLayoutManager: LinearLayoutManager

    private lateinit var headInfoListener: AppValueEventListener
    private lateinit var messageListener: AppChildrenEventListener

    private lateinit var receivingUser: User

    private lateinit var userDbRef: DatabaseReference
    private lateinit var messagesDbRef: DatabaseReference

    private var mCountMessenger: Int = 10
    private var mIsScrolling: Boolean = false
    private var mSmoothScrollToPosition = true

    override fun getViewBinding() = FragmentSingleChatBinding.inflate(layoutInflater)

    override fun onResume() {
        super.onResume()

        if (chatFromContacts != FROM_CHAT) saveToChatlist(contact.id, contact.namefromcontacts)

        mBottomNavigation = activity?.findViewById(R.id.bottomNav)
        mBottomNavigation?.visibility = View.GONE

        mLayoutManager = LinearLayoutManager(this.context)
        mLayoutManager.stackFromEnd = true

        headInfoListener = AppValueEventListener { data ->
            receivingUser = data.getUser()
            updateToolbarInfo()
        }

        userDbRef = mApplication.databaseFbRef.child(NODE_USERS).child(contact.id)
        userDbRef.addValueEventListener(headInfoListener)
        binding.backBtn.setOnClickListener {
            appActivity.supportFragmentManager.popBackStack()
        }

        binding.sendMessageBtn.setOnClickListener {
            initHeaderInfo()
        }

        binding.toolbarImg.setOnClickListener {
            replaceFragment(
                SettingsFragment(
                    contact.username,
                    contact.fullname,
                    contact.phone,
                    contact.bio,
                    contact.photoUrl,
                    true
                )
            )
        }
        initRecyclerView()

        binding.menuBtn.setOnClickListener {
            showMenu(binding.menuBtn)
        }
    }

    private fun initRecyclerView() {

        mRecyclerView = binding.rcMessage
        mAdapter = SingleChatAdapter()
        messagesDbRef = mApplication.databaseFbRef.child(NODE_MESSAGES)
            .child(mApplication.currentUserID)
            .child(contact.id)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.isNestedScrollingEnabled = false
        mRecyclerView.layoutManager = mLayoutManager

        messageListener = AppChildrenEventListener {
            val message = it.getCommonModel()
            if (mSmoothScrollToPosition) {
                mAdapter.addItemToBottom(message) {
                    mRecyclerView.smoothScrollToPosition(mAdapter.itemCount)
                }
            } else {
                mAdapter.addItemToTop(message) {
                    binding.chatSwipe.isRefreshing = false
                }
            }
        }
        messagesDbRef.removeEventListener(messageListener)
        messagesDbRef.limitToLast(mCountMessenger).addChildEventListener(messageListener)


        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (mIsScrolling && dy < 0 && mLayoutManager.findFirstVisibleItemPosition() <= 3) {
                    updateData()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
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
        messagesDbRef.removeEventListener(messageListener)
        messagesDbRef.limitToLast(mCountMessenger).addChildEventListener(messageListener)
    }

    private fun initHeaderInfo() {
        mSmoothScrollToPosition = true
        val message = binding.chatInputMessage.text.toString()
        if (message.isEmpty()) {
            showToast("Введите текст!")
        } else sentMessage(message, contact.id, TYPE_TEXT) {
            binding.chatInputMessage.setText("")
            saveToChatlist(contact.id, contact.namefromcontacts, TYPE_TEXT)
        }
    }

    private fun sentMessage(
        message: String,
        receivingUserID: String,
        typeText: String,
        function: () -> Unit
    ) {
        val refDialogUser = "$NODE_MESSAGES/${mApplication.currentUserID}/$receivingUserID"
        val refReceiveDialogUser = "$NODE_MESSAGES/$receivingUserID/${mApplication.currentUserID}"

        val messageKey = mApplication.databaseFbRef.child(refDialogUser).push().key

        val mapMessage = hashMapOf<String, Any>()
        mapMessage[CHILD_FROM] = mApplication.currentUserID
        mapMessage[CHILD_TYPE] = typeText
        mapMessage[CHILD_TEXT] = message
        mapMessage[CHILD_ID] = messageKey.toString()
        mapMessage[CHILD_TIMESTAMP] = ServerValue.TIMESTAMP
        mapMessage[CHILD_MESSAGE_STATUS] = "не прочитано"

        val mapDialog = hashMapOf<String, Any>()
        mapDialog["$refDialogUser/$messageKey"] = mapMessage
        mapDialog["$refReceiveDialogUser/$messageKey"] = mapMessage

        mApplication.databaseFbRef
            .updateChildren(mapDialog)
            .addOnSuccessListener { function() }
            .addOnFailureListener { showToast(it.message.toString()) }

        FirebaseService.sharedPref =
            appActivity.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId
            .addOnFailureListener { it.message.toString() }
            .addOnSuccessListener {
                FirebaseService.token = it.token
            }

        val title = "От"
        if (title.isNotEmpty() && message.isNotEmpty() && receivingUserID.isNotEmpty()) {
            PushNotification(
                NotificationData("from: ${phoneFormat(mApplication.currentUser.phone)}", message),
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
        mBottomNavigation?.visibility = View.VISIBLE
        userDbRef.removeEventListener(headInfoListener)
        messagesDbRef.removeEventListener(messageListener)
    }

    fun showMenu(view: View) {
        val popup = PopupMenu(appActivity, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.single_chat_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.clear_chat -> {
                    clearChat(contact.id) {
                        showToast("Chat was cleared")
                        replaceFragment(ChatFragment())
                    }
                }
                R.id.delete_chat -> {
                    deleteChat(contact.id) {
                        showToast("Chat was deleted")
                        replaceFragment(ChatFragment())
                    }
                }
            }
            true
        }
        popup.show()
    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
            } catch (e: Exception) {
                showToast(e.message.toString())
            }
        }
}