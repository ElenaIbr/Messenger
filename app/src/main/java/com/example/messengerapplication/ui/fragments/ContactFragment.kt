package com.example.messengerapplication.ui.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger.ui.fragments.BaseFragment
import com.example.messengerapplication.R
import com.example.messengerapplication.models.CommonModel
import com.example.messengerapplication.ui.fragments.single_chat.SingleChatFragment
import com.example.messengerapplication.utilits.*
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import de.hdodenhof.circleimageview.CircleImageView

class ContactFragment : BaseFragment(R.layout.fragment_contact) {

    private lateinit var rcView: RecyclerView
    private lateinit var adapter: FirebaseRecyclerAdapter<CommonModel, Holder>
    private lateinit var contactsRef: DatabaseReference
    private lateinit var userRef: DatabaseReference

    override fun onResume() {
        super.onResume()
        initRcview()
    }

    private fun initRcview() {
        rcView = APP_ACTIVITY.findViewById(R.id.contact_rc)
        contactsRef = REF_DATABASE_ROOT.child(NODE_PHONES_CONTACTS).child(UID)

        val options = FirebaseRecyclerOptions.Builder<CommonModel>()
            .setQuery(contactsRef, CommonModel::class.java)
            .build()

        adapter = object : FirebaseRecyclerAdapter<CommonModel, Holder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
                return Holder(view)
            }

            override fun onBindViewHolder(holder: Holder,
                                          position: Int,
                                          model: CommonModel
            ) {
                userRef = REF_DATABASE_ROOT.child(NODE_USERS).child(model.id)
                userRef.addValueEventListener(AppValueEventListener{
                    val contact = it.getValue(CommonModel::class.java)?:CommonModel()

                    if(contact.fullname.isEmpty()){
                        holder.name.text = model.fullname
                    }else holder.name.text = contact.fullname

                    holder.status.text = contact.state
                    holder.photo.setImg(contact.photoUrl)
                    holder.itemView.setOnClickListener {
                        replaceFragment(SingleChatFragment(model))
                    }
                })
            }
        }
        rcView.adapter = adapter
        adapter.startListening()

    }

    class Holder(view: View): RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.contact_name)
        val status: TextView = view.findViewById(R.id.contact_status)
        val photo: CircleImageView = view.findViewById(R.id.contact_photo)
    }

    override fun onPause() {
        super.onPause()
        adapter.stopListening()
    }

}