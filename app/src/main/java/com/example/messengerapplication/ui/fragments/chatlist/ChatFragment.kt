package com.example.messengerapplication.ui.fragments.chatlist

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapplication.databinding.FragmentChatBinding
import com.example.messengerapplication.models.CommonModel
import com.example.messengerapplication.ui.fragments.BaseFragment
import com.example.messengerapplication.utilits.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import java.util.*


class ChatFragment : BaseFragment<FragmentChatBinding>() {

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatDbRef: DatabaseReference
    private lateinit var usersDbRef: DatabaseReference
    private lateinit var messagesDbRef: DatabaseReference
    private lateinit var messagesFromChat: List<CommonModel>
    private lateinit var sortedMessages: List<CommonModel>
    private lateinit var messages: List<CommonModel>

    override fun getViewBinding() = FragmentChatBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            chatDbRef = mApplication.databaseFbRef
                .child(NODE_CHATLIST)
                .child(mApplication.currentUserID)
            usersDbRef = mApplication.databaseFbRef
                .child(NODE_USERS)
            messagesDbRef = mApplication.databaseFbRef
                .child(NODE_MESSAGES)
                .child(mApplication.currentUserID)
        }

        chatRecyclerView = binding.chatlistRc
        chatAdapter = ChatAdapter()

        initItemsForChat(chatAdapter, chatRecyclerView)
    }

    override fun onResume() {
        super.onResume()
        countMessages()
    }

    private fun initItemsForChat(chatAdapter: ChatAdapter, chatRecyclerView: RecyclerView) {

        chatDbRef.addListenerForSingleValueEvent(AppValueEventListener { chat ->
            messagesFromChat = chat.children.map { item -> item.getCommonModel() }

            messagesFromChat.forEach { message ->
                if (message.lastMessageTime != "") message.resLastMessageTime =
                    Date(message.lastMessageTime.toString().toLong())
            }

            sortedMessages =
                messagesFromChat.sortedBy { item -> item.resLastMessageTime }.reversed()

            sortedMessages.forEach { model ->

                readData2(object : firebaseCallback {
                    override fun onCallback(value: Double?) {

                        val unreadMessages = messgeCount2

                        usersDbRef.child(model.id)
                            .addListenerForSingleValueEvent(AppValueEventListener {
                                val newModel = it.getCommonModel()

                                messagesDbRef.child(model.id).limitToLast(1)
                                    .addListenerForSingleValueEvent(AppValueEventListener { lastMessage ->
                                        messages =
                                            lastMessage.children.map { item -> item.getCommonModel() }
                                        if (messages.isEmpty()) {
                                            newModel.lastMessage = "Chat was cleared"
                                        } else if (messages[0].text.length > 30) {
                                            newModel.lastMessage =
                                                messages[0].text.substring(0, 29) + "..."
                                            newModel.timeStamp = messages[0].timeStamp
                                        } else {
                                            newModel.lastMessage = messages[0].text
                                            newModel.timeStamp = messages[0].timeStamp
                                        }

                                        newModel.namefromcontacts =
                                            contactNamesFromDevice[newModel.phone].toString()

                                        if (newModel.namefromcontacts == "null") {
                                            newModel.namefromcontacts = newModel.phone
                                        }

                                        newModel.messageCount = unreadMessages.toInt()
                                        chatAdapter.updateListItem(newModel)
                                    })
                            })
                    }
                }, model.id)
            }
        })
        chatRecyclerView.adapter = chatAdapter
    }

    private fun countMessages() {

        val nameRef =
            mApplication.databaseFbRef.child(NODE_CHATLIST).child(mApplication.currentUserID)
        val eventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var count = 0.0
                for (ds in dataSnapshot.children) {
                    val rating = ds.child("messageCount").getValue(Double::class.java)!!
                    count += rating
                }
                if (count != 0.0) addBadge(count.toInt())
                else removeBadge()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        nameRef.addListenerForSingleValueEvent(eventListener)
    }

}