package com.pvsrishabh.momentshub.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.Models.Story
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.databinding.ActivityStoryBinding
import com.pvsrishabh.momentshub.models.Message
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.utils.CHAT
import com.pvsrishabh.momentshub.utils.STORY
import com.pvsrishabh.momentshub.utils.STORYLIKE
import com.pvsrishabh.momentshub.utils.USER_NODE
import com.squareup.picasso.Picasso

class StoryActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityStoryBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (intent.hasExtra("uid")) {
            val uid = intent.getStringExtra("uid")!!

            if(Firebase.auth.currentUser!!.uid == uid){
                binding.etMessage.visibility = View.GONE
                binding.like.visibility = View.GONE
                binding.send.visibility = View.GONE
            }

            Firebase.firestore.collection(USER_NODE).document(uid).get().addOnSuccessListener {
                val user = it.toObject<User>()!!
                Picasso.get().load(user.image).placeholder(R.drawable.user)
                    .into(binding.profile)
                binding.userName.text = user.name
                Firebase.firestore.collection(STORY).document(uid + STORY).get()
                    .addOnSuccessListener { st ->
                        val story = st.toObject<Story>()!!
                        binding.videoView.setVideoPath(story.videoUrl)
                        binding.videoView.setOnPreparedListener { mp ->
                            binding.progressBar.visibility = View.GONE
                            mp.isLooping = true // Set the video to loop
                            binding.videoView.start()
                        }
                        // Set on completion listener to restart video playback
                        binding.videoView.setOnCompletionListener { mp ->
                            mp.start()
                        }
                    }
            }

            // message

            val db = FirebaseFirestore.getInstance()

            val senderId = FirebaseAuth.getInstance().uid!!
            val receiveId = uid

            val senderRoom = "$senderId$receiveId"
            val receiverRoom = "$receiveId$senderId"

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
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@StoryActivity,
                                    "Message Sent",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
            }


            // like

            var like = 0

            db.collection(senderId + STORYLIKE)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener {
                    if (it.isEmpty) {
                        like = 0
                        binding.like.setImageResource(R.drawable.like)
                    } else {
                        like = 1
                        binding.like.setImageResource(R.drawable.heart)
                    }
                }

            val data = hashMapOf(
                "userId" to uid
            )

            binding.like.setOnClickListener {
                if (like == 0) {
                    binding.like.setImageResource(R.drawable.heart)
                    db.collection(senderId + STORYLIKE)
                        .document().set(data)
                        .addOnSuccessListener {
                            like = 1
                        }
                } else {
                    binding.like.setImageResource(R.drawable.like)
                    db.collection(senderId + STORYLIKE)
                        .whereEqualTo("userId", uid).get().addOnSuccessListener { querySnapshot ->
                            // Iterate through the documents that match the query
                            for (document in querySnapshot.documents) {
                                // Delete each matching document
                                db.collection(senderId + STORYLIKE).document(document.id)
                                    .delete().addOnSuccessListener {
                                        like = 0
                                    }
                            }
                        }
                }
            }
        }
    }
}