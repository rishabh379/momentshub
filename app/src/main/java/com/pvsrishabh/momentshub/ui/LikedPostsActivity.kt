package com.pvsrishabh.momentshub.ui

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
import com.pvsrishabh.momentshub.databinding.ActivityLikedPostsBinding
import com.pvsrishabh.momentshub.models.Post
import com.pvsrishabh.momentshub.utils.LIKE

class LikedPostsActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLikedPostsBinding.inflate(layoutInflater)
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
                Intent(this@LikedPostsActivity, HomeActivity::class.java)
            )
            finish()
        }

        val requestManager: RequestManager = Glide.with(this@LikedPostsActivity)

        binding.rvLikedPost.layoutManager = LinearLayoutManager(this@LikedPostsActivity)
        adapter = PostAdapter(this@LikedPostsActivity, postList, requestManager)
        binding.rvLikedPost.adapter = adapter

        val currentUser: String = Firebase.auth.currentUser!!.uid
        Firebase.firestore.collection(currentUser + LIKE).get().addOnSuccessListener {
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
}