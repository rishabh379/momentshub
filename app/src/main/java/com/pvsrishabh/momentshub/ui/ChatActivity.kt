package com.pvsrishabh.momentshub.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.utils.FOLLOW
import com.pvsrishabh.momentshub.adapters.MessageAdapter
import com.pvsrishabh.momentshub.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {
    private val binding by lazy{
        ActivityChatBinding.inflate(layoutInflater)
    }
    lateinit var adapter: MessageAdapter
    private var userList = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        setSupportActionBar(binding.materialToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.materialToolbar.setNavigationOnClickListener {
            startActivity(
                Intent(this@ChatActivity, HomeActivity::class.java)
            )
            finish()
        }

        binding.rvMsg.layoutManager = LinearLayoutManager(this)
        adapter= MessageAdapter(this,userList)
        binding.rvMsg.adapter = adapter

        val currentUser: String = Firebase.auth.currentUser!!.uid
        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW).get().addOnSuccessListener {
            val tempList = ArrayList<User>()
            userList.clear()
            for(i in it.documents){
                if(i.id != currentUser) {
                    val user = i.toObject<User>()!!
                    tempList.add(user)
                }
            }
            userList.addAll(tempList)
            adapter.notifyDataSetChanged()
        }

    }
}