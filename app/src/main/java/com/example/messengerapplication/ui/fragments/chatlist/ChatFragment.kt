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
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.coroutineScope
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

        /*viewLifecycleOwner.lifecycleScope.launch {
            val job = launch{
                chatRecyclerView = binding.chatlistRc
                chatAdapter = ChatlistAdapter()

                initItemsForChatlist(chatAdapter, chatRecyclerView)
                replaceFragment(ChatFragment())
            }
            replaceFragment(WaitingFragment())
            job.join()
        }*/
    }

    override fun onResume() {
        super.onResume()
        //initRecyclerView()
    }


    private fun initRecyclerView() {


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

                refUsers.child(model.id).addListenerForSingleValueEvent(AppValueEventListener {
                    val newModel = it.getCommonModel()

                    //newModel.namefromcontacts = model.namefromcontacts

                    refMesseges.child(model.id).limitToLast(1)
                        .addListenerForSingleValueEvent(AppValueEventListener {
                            messageList = it.children.map { it.getCommonModel() }
                            if (messageList.isEmpty()) {
                                newModel.lastMessage = "Чат очищен"
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
                            chatAdapter.updateListIten(newModel)
                        })

                })
            }
        })
        chatRecyclerView.adapter = chatAdapter
    }


}