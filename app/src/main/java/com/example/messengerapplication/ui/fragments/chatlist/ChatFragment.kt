package com.example.messengerapplication.ui.fragments.chatlist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapplication.R
import com.example.messengerapplication.databinding.FragmentChatBinding
import com.example.messengerapplication.databinding.FragmentContactBinding
import com.example.messengerapplication.models.CommonModel
import com.example.messengerapplication.utilits.*

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatAdapter: ChatlistAdapter
    private lateinit var chatRecyclerView: RecyclerView
    private val refChatlist = REF_DATABASE_ROOT.child(NODE_CHATLIST).child(UID)
    private val refUsers = REF_DATABASE_ROOT.child(NODE_USERS)
    private val refMesseges = REF_DATABASE_ROOT.child(NODE_MESSAGES).child(UID)
    private var listItem = listOf<CommonModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        chatRecyclerView = binding.chatlistRc
        chatAdapter = ChatlistAdapter()

        refChatlist.addListenerForSingleValueEvent(AppValueEventListener{
            listItem = it.children.map { it.getCommonModel() }

            listItem.forEach { model ->
                refUsers.child(model.id).addListenerForSingleValueEvent(AppValueEventListener{
                    val newModel = it.getCommonModel()

                    refMesseges.child(model.id).limitToLast(1).addListenerForSingleValueEvent(AppValueEventListener{
                        val messageList = it.children.map { it.getCommonModel() }
                        newModel.lastMessage = messageList[0].text

                        if(newModel.fullname.isEmpty()){
                            newModel.fullname = newModel.phone
                        }
                        chatAdapter.updateListIten(newModel)
                    })

                })
            }
        })

        chatRecyclerView.adapter = chatAdapter

    }

}