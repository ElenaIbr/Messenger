package com.example.messengerapplication.ui.fragments.single_chat

import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapplication.R
import com.example.messengerapplication.databinding.FragmentSingleChatBinding
import com.example.messengerapplication.models.CommonModel
import com.example.messengerapplication.models.User
import com.example.messengerapplication.utilits.*
import com.google.firebase.database.*


class SingleChatFragment(val contact: CommonModel) : Fragment(R.layout.fragment_single_chat) {

    private lateinit var binding: FragmentSingleChatBinding
    private var act: View? = null
    private lateinit var headInfoListener: AppValueEventListener
    private lateinit var receivingUser: User
    private lateinit var userRef: DatabaseReference

    private lateinit var messagesRef: DatabaseReference
    private lateinit var adapter: SingleChatAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageListener: AppChildrenEventListener

    private var mCountMessenger: Int = 10
    private var mIsScrolling: Boolean = false
    private var mSmoothScrollToPosition = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSingleChatBinding.bind(view)
    }

    override fun onResume() {
        super.onResume()

        act = activity?.findViewById(R.id.bottomNav)
        act?.visibility = View.GONE

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

        initRecyclerView()
    }

    private fun initRecyclerView() {

        recyclerView = binding.rcMessage
        adapter = SingleChatAdapter()
        messagesRef = REF_DATABASE_ROOT.child(NODE_MESSAGES)
            .child(UID)
            .child(contact.id)
        recyclerView.adapter = adapter

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
                if(mIsScrolling && dy < 0){
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

        val mapDialod = hashMapOf<String, Any>()
        mapDialod["$refDialogUser/$messageKey"] = mapMessage
        mapDialod["$refReceivDialogUser/$messageKey"] = mapMessage

        REF_DATABASE_ROOT
            .updateChildren(mapDialod)
            .addOnSuccessListener { function() }
            .addOnFailureListener { showToast(it.message.toString()) }
    }

    private fun updateToolbarInfo() {
        binding.toolbarImg.setImg(receivingUser.photoUrl)

        if(receivingUser.fullname.isEmpty()){
            binding.toolbarContactName.text = contact.fullname
        }else binding.toolbarContactName.text = receivingUser.username

        binding.toolbarContactStatus.text = receivingUser.state
    }

    override fun onPause() {
        super.onPause()
        act?.visibility = View.VISIBLE
        userRef.removeEventListener(headInfoListener)
        messagesRef.removeEventListener(messageListener)
    }
}