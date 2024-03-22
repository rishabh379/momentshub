package com.pvsrishabh.momentshub.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.adapters.PostAdapter
import com.pvsrishabh.momentshub.databinding.ActivitySavedPostsBinding
import com.pvsrishabh.momentshub.models.Post
import com.pvsrishabh.momentshub.utils.SAVE

class SavedPostsActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySavedPostsBinding.inflate(layoutInflater)
    }

    lateinit var adapter: PostAdapter

    private var postList = ArrayList<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.materialToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.materialToolbar.setNavigationOnClickListener {
            startActivity(
                Intent(this@SavedPostsActivity, HomeActivity::class.java)
            )
            finish()
        }

        val requestManager: RequestManager = Glide.with(this@SavedPostsActivity)

        binding.rvSavedPost.layoutManager = LinearLayoutManager(this@SavedPostsActivity)
        adapter = PostAdapter(this@SavedPostsActivity, postList, requestManager)
        binding.rvSavedPost.adapter = adapter

        val currentUser: String = Firebase.auth.currentUser!!.uid
        Firebase.firestore.collection(currentUser + SAVE).get().addOnSuccessListener {
            val tempList = ArrayList<Post>()
            postList.clear()
            for (i in it.documents) {
                val post = i.toObject<Post>()!!
                tempList.add(post)
            }
            postList.addAll(tempList)
            adapter.notifyDataSetChanged()
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Perform any necessary actions on back press, such as closing resources or dialogs
        super.onBackPressed()
        startActivity(Intent(this@SavedPostsActivity, HomeActivity::class.java))
    }
}