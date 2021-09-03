package com.example.messengerapplication.ui.fragments.chatlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapplication.R
import com.example.messengerapplication.models.CommonModel
import com.example.messengerapplication.utilits.setImg
import de.hdodenhof.circleimageview.CircleImageView

class ChatlistAdapter : RecyclerView.Adapter<ChatlistAdapter.ChatlistHolder>() {

    private var listItem = mutableListOf<CommonModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatlistHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chatlist_item, parent, false)
        return ChatlistHolder(view)
    }

    override fun onBindViewHolder(holder: ChatlistHolder, position: Int) {
        holder.contactName.text = listItem[position].fullname
        holder.lastMessage.text = listItem[position].lastMessage
        holder.contactPhoto.setImg(listItem[position].photoUrl)
    }

    override fun getItemCount(): Int = listItem.size

    class ChatlistHolder(view: View): RecyclerView.ViewHolder(view) {
        val contactName = view.findViewById<TextView>(R.id.chatlist_contact_name)
        val contactPhoto = view.findViewById<CircleImageView>(R.id.chatlist_contact_photo)
        val lastMessage = view.findViewById<TextView>(R.id.chatlist_last_message)
    }

    fun updateListIten(item: CommonModel){
        listItem.add(item)
        notifyItemInserted(listItem.size)
    }
}

