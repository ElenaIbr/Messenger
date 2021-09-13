package com.example.messengerapplication.ui.fragments.chatlist

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapplication.R
import com.example.messengerapplication.models.CommonModel
import com.example.messengerapplication.ui.fragments.single_chat.SingleChatFragment
import com.example.messengerapplication.utilits.*
import de.hdodenhof.circleimageview.CircleImageView

class ChatlistAdapter : RecyclerView.Adapter<ChatlistAdapter.ChatlistHolder>() {

    private var listItem = mutableListOf<CommonModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatlistHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chatlist_item, parent, false)
        return ChatlistHolder(view)
    }

    override fun onBindViewHolder(holder: ChatlistHolder, position: Int) {

        //holder.contactName.text = listItem[position].fullname
        holder.contactName.text = listItem[position].namefromcontacts
        holder.lastMessage.text = listItem[position].lastMessage
        holder.contactPhoto.setImg(listItem[position].photoUrl)

        if(listItem[position].messageCount!=0){
            //holder.messages.setImageResource(R.drawable.unread_message)
            holder.messages.visibility = View.VISIBLE
            holder.messages.text = listItem[position].messageCount.toString()
        }
        //else holder.messages.visibility = View.VISIBLE

        if(!listItem[position].timeStamp.toString().isEmpty()){
            holder.lastMessageTime.text = listItem[position]
                .timeStamp
                .toString()
                .toDateFormat()
        }else holder.lastMessageTime.text = ""

        holder.itemView.setOnClickListener {
            APP_ACTIVITY.changeFragment(SingleChatFragment(listItem[position]))
        }
    }

    override fun getItemCount(): Int = listItem.size

    class ChatlistHolder(view: View): RecyclerView.ViewHolder(view) {
        val contactName = view.findViewById<TextView>(R.id.chatlist_contact_name)
        val contactPhoto = view.findViewById<CircleImageView>(R.id.chatlist_contact_photo)
        val lastMessage = view.findViewById<TextView>(R.id.chatlist_last_message)
        val lastMessageTime = view.findViewById<TextView>(R.id.message_time)
        val messages = view.findViewById<TextView>(R.id.unread_mark)
    }

    fun updateListIten(item: CommonModel){
        listItem.add(item)
        notifyItemInserted(listItem.size)
    }
}

