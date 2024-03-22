package com.pvsrishabh.momentshub.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.adapters.SearchAdapter
import com.pvsrishabh.momentshub.databinding.ActivityFollowBinding
import com.pvsrishabh.momentshub.models.User

class FollowActivity : AppCompatActivity(), SearchAdapter.ErrorHandlingListener {
    // Other activity code...

    override fun handleErrorAndNavigate() {
        // Handle error and navigate to HomeActivity
        finish()  // Finish the current activity or fragment
        startActivity(Intent(this, HomeActivity::class.java))
    }

    private val binding by lazy {
        ActivityFollowBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: SearchAdapter
    private var userList = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        try {

            if (intent.hasExtra("uid") && intent.hasExtra("type")) {
                val uid = intent.getStringExtra("uid")
                val type = intent.getStringExtra("type")
                if (uid != null && type != null) {
                    val path: String = uid + type

                    binding.rv.layoutManager = LinearLayoutManager(this@FollowActivity)
                    adapter = SearchAdapter(this, userList, this)
                    binding.rv.adapter = adapter

                    loadUsers(path)

                    binding.searchButton.setOnClickListener {
                        val text = binding.userName.text.toString()
                        searchUsers(path, text)
                    }
                }
            }
        } catch (e: Exception) {
            // Handle any exceptions here
            Toast.makeText(this@FollowActivity, "An error occurred", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            startActivity(Intent(this@FollowActivity, HomeActivity::class.java))
        }
    }

    private fun loadUsers(path: String) {
        FirebaseFirestore.getInstance().collection(path).get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    val tempList = ArrayList<User>()
                    userList.clear()
                    for (document in it.documents) {
                        val user = document.toObject<User>()
                        user?.let {
                            tempList.add(user)
                        }
                    }
                    userList.addAll(tempList)
                    adapter.notifyDataSetChanged()
                }
            }.addOnFailureListener {
                Toast.makeText(this@FollowActivity, "Failed to load users", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun searchUsers(path: String, searchText: String) {
        FirebaseFirestore.getInstance().collection(path)
            .whereEqualTo("name", searchText).get()
            .addOnSuccessListener {
                val tempList = ArrayList<User>()
                userList.clear()
                if (!it.isEmpty) {
                    for (document in it.documents) {
                        val user = document.toObject<User>()
                        user?.let {
                            tempList.add(user)
                        }
                    }
                    userList.addAll(tempList)
                    adapter.notifyDataSetChanged()
                }
            }.addOnFailureListener {
                Toast.makeText(this@FollowActivity, "No users found", Toast.LENGTH_SHORT).show()
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Perform any necessary actions on back press, such as closing resources or dialogs
        super.onBackPressed()
        startActivity(Intent(this@FollowActivity, HomeActivity::class.java))
    }
}