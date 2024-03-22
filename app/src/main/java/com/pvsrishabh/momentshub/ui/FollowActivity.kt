package com.pvsrishabh.momentshub.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.adapters.SearchAdapter
import com.pvsrishabh.momentshub.databinding.ActivityFollowBinding
import com.pvsrishabh.momentshub.models.User

class FollowActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityFollowBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: SearchAdapter
    private var userList = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.rv.layoutManager = LinearLayoutManager(this@FollowActivity)
        adapter = SearchAdapter(this, userList)
        binding.rv.adapter = adapter
        val currUser = Firebase.auth.currentUser!!.uid

        if (intent.hasExtra("uid") && intent.hasExtra("type")) {
            val uid = intent.getStringExtra("uid")
            val type = intent.getStringExtra("type")
            val path: String = uid + type
            FirebaseFirestore.getInstance().collection(path).get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        val tempList = ArrayList<User>()
                        userList.clear()
                        for (document in it.documents) {
                            if (document.id != uid) {
                                val user = document.toObject<User>()
                                user?.let {
                                    tempList.add(user)
                                }
                            }
                        }
                        userList.addAll(tempList)
                        adapter.notifyDataSetChanged()
                    }

                }

            binding.searchButton.setOnClickListener {
                val text = binding.userName.text.toString()
                FirebaseFirestore.getInstance().collection(path)
                    .whereEqualTo("name", text).get()
                    .addOnSuccessListener {
                        val tempList = ArrayList<User>()
                        userList.clear()
                        if (!it.isEmpty) {
                            for (document in it.documents) {
                                if (document.id != uid) {
                                    val user = document.toObject<User>()
                                    user?.let {
                                        tempList.add(user)
                                    }
                                }
                            }
                            userList.addAll(tempList)
                            adapter.notifyDataSetChanged()
                        }
                    }
            }

        }
    }
}