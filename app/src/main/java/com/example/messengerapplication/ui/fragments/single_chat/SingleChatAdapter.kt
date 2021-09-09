package com.example.messengerapplication.ui.fragments.single_chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapplication.R
import com.example.messengerapplication.models.CommonModel
import com.example.messengerapplication.utilits.DiffUtilCallback
import com.example.messengerapplication.utilits.UID
import com.example.messengerapplication.utilits.toTimeFormat
import java.text.SimpleDateFormat
import java.util.*

class SingleChatAdapter : RecyclerView.Adapter<SingleChatAdapter.SingleChatHolder>() {

    private var mListMessagesCache = mutableListOf<CommonModel>()

    class SingleChatHolder(view: View) : RecyclerView.ViewHolder(view){

        val rightBlockMessage: LinearLayout = view.findViewById(R.id.right_block_message)
        val rightMessage: TextView = view.findViewById(R.id.right_message)
        val rightMessageTime: TextView = view.findViewById(R.id.right_message_time)

        val leftBlockMessage: LinearLayout = view.findViewById(R.id.left_block_message)
        val leftMessage: TextView = view.findViewById(R.id.left_message)
        val leftMessageTime: TextView = view.findViewById(R.id.left_message_time)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleChatHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return SingleChatHolder(view)
    }

    override fun onBindViewHolder(holder: SingleChatHolder, position: Int) {
        if(mListMessagesCache[position].from==UID){
            holder.rightBlockMessage.visibility = View.VISIBLE
            holder.leftBlockMessage.visibility = View.GONE
            holder.rightMessage.text = mListMessagesCache[position].text
            holder.rightMessageTime.text = mListMessagesCache[position]
                .timeStamp
                .toString()
                .toTimeFormat()
        }else{
            holder.rightBlockMessage.visibility = View.GONE
            holder.leftBlockMessage.visibility = View.VISIBLE
            holder.leftMessage.text = mListMessagesCache[position].text
            holder.leftMessageTime.text = mListMessagesCache[position]
                .timeStamp
                .toString()
                .toTimeFormat()
        }
    }

    override fun getItemCount(): Int = mListMessagesCache.size

    fun addItemToBottom(item: CommonModel,
                        onSuccess: () -> Unit){
        if(!mListMessagesCache.contains(item)){
            mListMessagesCache.add(item)
            notifyItemInserted(mListMessagesCache.size)
        }
        onSuccess()
    }

    fun addItemToTop(item: CommonModel,
                        onSuccess: () -> Unit){
        if(!mListMessagesCache.contains(item)){
            mListMessagesCache.add(item)
            mListMessagesCache.sortBy { it.timeStamp.toString() }
            notifyItemInserted(0)
        }
        onSuccess()
    }
}

