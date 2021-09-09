package com.example.messengerapplication.ui.fragments.chatlist

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapplication.R
import com.example.messengerapplication.databinding.FragmentChatBinding
import com.example.messengerapplication.databinding.FragmentContactBinding
import com.example.messengerapplication.models.CommonModel
import com.example.messengerapplication.ui.fragments.BaseFragment
import com.example.messengerapplication.utilits.*
import java.util.*

class ChatFragment : BaseFragment<FragmentChatBinding>() {

    private lateinit var chatAdapter: ChatlistAdapter
    private lateinit var chatRecyclerView: RecyclerView
    private val refChatlist = REF_DATABASE_ROOT.child(NODE_CHATLIST).child(UID)
    private val refUsers = REF_DATABASE_ROOT.child(NODE_USERS)
    private val refMesseges = REF_DATABASE_ROOT.child(NODE_MESSAGES).child(UID)
    private var listItem = listOf<CommonModel>()

    override fun getViewBinding() = FragmentChatBinding.inflate(layoutInflater)

    override fun onResume() {
        super.onResume()
        initRecyclerView()
    }

    private fun initRecyclerView() {

        chatRecyclerView = binding.chatlistRc
        chatAdapter = ChatlistAdapter()

        refChatlist.addListenerForSingleValueEvent(AppValueEventListener{
            listItem = it.children.map { it.getCommonModel() }

            listItem.forEach {
                if(it.lastMessageTime!="") it.resLastMessageTime = Date(it.lastMessageTime.toString().toLong())
            }

            val sortedList = listItem.sortedBy{ it.resLastMessageTime }.reversed()

            sortedList.forEach { model ->

                refUsers.child(model.id).addListenerForSingleValueEvent(AppValueEventListener{
                    val newModel = it.getCommonModel()

                    newModel.namefromcontacts = model.namefromcontacts

                    refMesseges.child(model.id).limitToLast(1).addListenerForSingleValueEvent(AppValueEventListener{
                        val messageList = it.children.map { it.getCommonModel() }
                        if(messageList.isEmpty()){
                            newModel.lastMessage = "Чат очищен"
                        }else if(messageList[0].text.length>30){
                            newModel.lastMessage = messageList[0].text.substring(0,29)+"..."
                            newModel.timeStamp = messageList[0].timeStamp
                        }else {
                            newModel.lastMessage = messageList[0].text
                            newModel.timeStamp = messageList[0].timeStamp
                        }
                        if(newModel.namefromcontacts.isEmpty()){
                            newModel.namefromcontacts = newModel.phone
                        }

                        chatAdapter.updateListIten(newModel)
                    })

                })
            }
        })
        chatRecyclerView.adapter = chatAdapter
    }
}