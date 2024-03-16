package com.pvsrishabh.momentshub.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pvsrishabh.momentshub.models.Message
import com.pvsrishabh.momentshub.utils.CHAT
import com.pvsrishabh.momentshub.adapters.ChatAdapter
import com.pvsrishabh.momentshub.databinding.ActivityDetailedChatBinding

class DetailedChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailedChatBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailedChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

// Set toolbar as action bar
        setSupportActionBar(binding.userNameToolbar)

// Enable Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

// Set navigation click listener
        binding.userNameToolbar.setNavigationOnClickListener {
            startActivity(Intent(this@DetailedChatActivity, ChatActivity::class.java))
            finish()
        }

// Retrieve and set title
        val userName = intent.getStringExtra("userName")
        if (userName != null) {
            supportActionBar?.title = userName
        }

        val senderId = FirebaseAuth.getInstance().uid!!
        val receiveId = intent.getStringExtra("userId")

        // Declare messageModels as mutableList instead of ArrayList
        val messageModels = ArrayList<Message>()
        val chatAdapter = ChatAdapter(messageModels, this, receiveId)
        binding.chatRecyclerView.adapter = chatAdapter

        val layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.layoutManager = layoutManager

        val senderRoom = "$senderId$receiveId"
        val receiverRoom = "$receiveId$senderId"

        val db = FirebaseFirestore.getInstance()

// Listener for retrieving messages from Firestore
        val query = db.collection(CHAT)
            .document(senderRoom)
            .collection("messages")
            .orderBy("time", Query.Direction.ASCENDING)

        val registration = query.addSnapshotListener { value, error ->
            if (error != null) {
                // Handle error
                return@addSnapshotListener
            }

            val tempList = arrayListOf<Message>()
            for (document in value!!.documents) {
                val message = document.toObject(Message::class.java)
                message?.uId = document.id
                if (message != null) {
                    tempList.add(message)
                }
            }
            messageModels.clear()
            messageModels.addAll(tempList)
            chatAdapter.notifyDataSetChanged()
        }

        binding.send.setOnClickListener {
            if (binding.etMessage.text.toString().isEmpty()) {
                binding.etMessage.error = "Enter Your Message"
                return@setOnClickListener
            }

            val message = binding.etMessage.text.toString()
            val model = Message(senderId, message, System.currentTimeMillis().toString())
            binding.etMessage.setText("")

            // Push message to sender's and receiver's rooms in Firestore
            db.collection(CHAT)
                .document(senderRoom)
                .collection("messages")
                .add(model)
                .addOnSuccessListener {
                    db.collection(CHAT)
                        .document(receiverRoom)
                        .collection("messages")
                        .add(model)
                }

        }

        binding.shareImg.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 33)
        }

    }
}