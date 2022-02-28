package com.example.messengerapplication.ui.fragments.chatlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapplication.R
import com.example.messengerapplication.features.chat.domain.entity.CommonModel
import com.example.messengerapplication.ui.fragments.single_chat.SingleChatFragment
import com.example.messengerapplication.utilits.appActivity
import com.example.messengerapplication.utilits.changeFragment
import com.example.messengerapplication.utilits.setImg
import com.example.messengerapplication.utilits.toDateFormat
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatHolder>() {

    private var listItem = mutableListOf<CommonModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.chatlist_item, parent, false)
        return ChatHolder(view)
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {

        holder.contactName.text = listItem[position].namefromcontacts
        holder.lastMessage.text = listItem[position].lastMessage
        holder.contactPhoto.setImg(listItem[position].photoUrl)

        if (listItem[position].messageCount != 0) {
            holder.messages.visibility = View.VISIBLE
            holder.messages.text = listItem[position].messageCount.toString()
        }

        if (!listItem[position].timeStamp.toString().isEmpty()) {
            holder.lastMessageTime.text = listItem[position]
                .timeStamp
                .toString()
                .toDateFormat()
        } else holder.lastMessageTime.text = ""

        if(listItem[position].state == "online") holder.state.visibility = View.VISIBLE

        holder.itemView.setOnClickListener {
            appActivity.changeFragment(SingleChatFragment(listItem[position]))
        }
    }

    override fun getItemCount(): Int = listItem.size

    class ChatHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contactName: TextView = view.findViewById(R.id.chatlist_contact_name)
        val contactPhoto: CircleImageView = view.findViewById(R.id.chatlist_contact_photo)
        val lastMessage: TextView = view.findViewById(R.id.chatlist_last_message)
        val lastMessageTime: TextView = view.findViewById(R.id.message_time)
        val messages: TextView = view.findViewById(R.id.unread_mark)
        val state: ImageView = view.findViewById(R.id.user_state)
    }

    fun updateListItem(item: CommonModel) {
        listItem.add(item)
        notifyItemInserted(listItem.size)
    }
}

