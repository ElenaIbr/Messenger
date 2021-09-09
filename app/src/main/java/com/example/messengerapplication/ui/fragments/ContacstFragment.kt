package com.example.messengerapplication.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapplication.R
import com.example.messengerapplication.databinding.FragmentContactBinding
import com.example.messengerapplication.models.CommonModel
import com.example.messengerapplication.ui.fragments.single_chat.SingleChatFragment
import com.example.messengerapplication.utilits.*
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView


class ContacstFragment : BaseFragment<FragmentContactBinding>() {

    private lateinit var rcView: RecyclerView
    private lateinit var adapter: FirebaseRecyclerAdapter<CommonModel, Holder>
    private lateinit var contactsRef: DatabaseReference
    private lateinit var userRef: DatabaseReference

    override fun getViewBinding() = FragmentContactBinding.inflate(layoutInflater)



    override fun onResume() {
        super.onResume()
        initRcview("")

        binding.searchEdittext.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                initRcview(s.toString().lowercase())

            }
        })

    }

    private fun initRcview(searchText: String) {
        rcView = APP_ACTIVITY.findViewById(R.id.contact_rc)
        contactsRef = REF_DATABASE_ROOT.child(NODE_PHONES_CONTACTS).child(UID)


        val options = FirebaseRecyclerOptions.Builder<CommonModel>()
            .setQuery(contactsRef.orderByChild(CHILD_FULLNAME_LOWCASE )
                .startAt(searchText)
                .endAt(searchText + "\ufbff"), CommonModel::class.java)
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

                    /*if(contact.fullname.isEmpty()){
                        holder.name.text = model.fullname
                    }else holder.name.text = contact.fullname*/

                    holder.name.text = model.fullname

                    contact.namefromcontacts = model.fullname

                    Log.d("MyLog", "контакты ${model.fullname}")
                    //holder.status.text = phoneFormat(contact.phone)
                    holder.photo.setImg(contact.photoUrl)
                    holder.itemView.setOnClickListener {
                        replaceFragment(SingleChatFragment(contact))
                    }
                })
            }
        }
        rcView.adapter = adapter
        adapter.startListening()
    }

    class Holder(view: View): RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.contact_name)
        //val status: TextView = view.findViewById(R.id.contact_status)
        val photo: CircleImageView = view.findViewById(R.id.contact_photo)
    }

    override fun onPause() {
        super.onPause()
        adapter.stopListening()
    }

}