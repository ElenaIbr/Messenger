package com.example.messengerapplication.ui.fragments.chatlist

import android.os.Bundle
import android.util.Log
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

    private lateinit var chatAdapter: ChatlistAdapter
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var refChatlist: DatabaseReference
    private lateinit var refUsers: DatabaseReference
    private lateinit var refMesseges: DatabaseReference
    private lateinit var listItem: List<CommonModel>
    private lateinit var sortedList: List<CommonModel>
    private lateinit var messageList: List<CommonModel>



    override fun getViewBinding() = FragmentChatBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            refChatlist = REF_DATABASE_ROOT.child(NODE_CHATLIST).child(UID)
            refUsers = REF_DATABASE_ROOT.child(NODE_USERS)
            refMesseges = REF_DATABASE_ROOT.child(NODE_MESSAGES).child(UID)
        }

        chatRecyclerView = binding.chatlistRc
        chatAdapter = ChatlistAdapter()

        initItemsForChatlist(chatAdapter, chatRecyclerView)
    }

    override fun onResume() {
        super.onResume()
        countMessages()
    }

    fun initItemsForChatlist(chatAdapter: ChatlistAdapter, chatRecyclerView: RecyclerView) {

        refChatlist.addListenerForSingleValueEvent(AppValueEventListener {
            listItem = it.children.map { it.getCommonModel() }

            listItem.forEach {
                if (it.lastMessageTime != "") it.resLastMessageTime =
                    Date(it.lastMessageTime.toString().toLong())
            }

            sortedList = listItem.sortedBy { it.resLastMessageTime }.reversed()

            sortedList.forEach { model ->

                Log.d("MyLog", "id ${model.id}")


                readData2(object : firebaseCallback {
                    override fun onCallback(value: Double?) {

                        val unreadedMessages = messgeCount2

                        refUsers.child(model.id).addListenerForSingleValueEvent(AppValueEventListener {
                            val newModel = it.getCommonModel()

                            refMesseges.child(model.id).limitToLast(1)
                                .addListenerForSingleValueEvent(AppValueEventListener {
                                    messageList = it.children.map { it.getCommonModel() }
                                    if (messageList.isEmpty()) {
                                        newModel.lastMessage = "Chat cleared"
                                    } else if (messageList[0].text.length > 30) {
                                        newModel.lastMessage = messageList[0].text.substring(0, 29) + "..."
                                        newModel.timeStamp = messageList[0].timeStamp
                                    } else {
                                        newModel.lastMessage = messageList[0].text
                                        newModel.timeStamp = messageList[0].timeStamp
                                    }

                                    newModel.namefromcontacts = contactNames[newModel.phone].toString()

                                    if (newModel.namefromcontacts == "null") {
                                        newModel.namefromcontacts = newModel.phone
                                    }

                                    newModel.messageCount = unreadedMessages.toInt()
                                    chatAdapter.updateListIten(newModel)
                                })
                        })
                    }
                }, model.id)
            }
        })
        chatRecyclerView.adapter = chatAdapter
    }

    fun countMessages(){

        val nameRef = REF_DATABASE_ROOT.child(NODE_CHATLIST).child(UID)
        val eventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var count = 0.0
                for (ds in dataSnapshot.children) {
                    val rating = ds.child("messageCount").getValue(Double::class.java)!!
                    count += rating
                }
                if(count!=0.0) addBadge(count.toInt())
                else removeBadge()
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        nameRef.addListenerForSingleValueEvent(eventListener)
    }

}