package com.pvsrishabh.momentshub.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.adapters.ViewPagerAdapter
import com.pvsrishabh.momentshub.databinding.ActivityOthersProfileBinding
import com.pvsrishabh.momentshub.fragments.MyPostFragment
import com.pvsrishabh.momentshub.fragments.MyReelsFragment
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.utils.FOLLOW
import com.pvsrishabh.momentshub.utils.POST
import com.pvsrishabh.momentshub.utils.USER_NODE
import com.pvsrishabh.momentshub.utils.changeFollowersCount
import com.pvsrishabh.momentshub.utils.changeFollowingCount
import com.squareup.picasso.Picasso

class OthersProfileActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityOthersProfileBinding.inflate(layoutInflater)
    }
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        setSupportActionBar(binding.materialToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.materialToolbar.setNavigationOnClickListener {
            startActivity(
                Intent(this@OthersProfileActivity, HomeActivity::class.java)
            )
            finish()
        }

        if (intent.hasExtra("uid")) {

            val uid = intent.getStringExtra("uid")
            var userName: String? = ""

            viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
            viewPagerAdapter.addFragment(MyPostFragment(uid!!), "Posts")
            viewPagerAdapter.addFragment(MyReelsFragment(uid), "Reels")
            binding.viewPager.adapter = viewPagerAdapter
            binding.tabLayout.setupWithViewPager(binding.viewPager)

            val db = Firebase.firestore

            db.collection(USER_NODE).document(uid).get()
                .addOnSuccessListener {
                    val user: User = it.toObject<User>()!!
                    binding.materialToolbar.title = user.name
                    binding.tvName.text = user.name
                    userName = user.name
                    if (user.bio == null) {
                        binding.tvBio.text = user.email
                    } else {
                        binding.tvBio.text = user.bio
                    }
                    binding.followingCount.text = (user.followingCount?: 0).toString()
                    binding.postCount.text = (user.postCount?: 0).toString()
                    binding.followersCount.text = (user.followersCount?: 0).toString()
                    if (!user.image.isNullOrEmpty()) {
                        Picasso.get().load(user.image).into(binding.profileImage)
                    }
                }

            var isFollow = false
            Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW)
                .whereEqualTo("userId", uid).get().addOnSuccessListener {
                    if (it.documents.size != 0) {
                        binding.btnFollow.text = "Following"
                        binding.btnMessage.visibility = View.VISIBLE
                        binding.btnFollow.backgroundTintList =
                            ContextCompat.getColorStateList(this, R.color.gray)
                        isFollow = true
                    } else {
                        isFollow = false
                        binding.btnMessage.visibility = View.GONE
                    }
                }

            binding.btnFollow.setOnClickListener {
                if (isFollow) {
                    Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW)
                        .whereEqualTo("userId", uid).get().addOnSuccessListener {

                            Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW)
                                .document(it.documents[0].id).delete()
                            binding.btnFollow.text = "Follow"

                            val currentCountText = binding.followersCount.text.toString()
                            val currentCount = currentCountText.toIntOrNull() ?: 0
                            val newCount = currentCount - 1
                            binding.followersCount.text = newCount.toString()

                            changeFollowersCount(-1,uid)
                            changeFollowingCount(-1)
                            binding.btnMessage.visibility = View.GONE
                            binding.btnFollow.backgroundTintList =
                                ContextCompat.getColorStateList(this, R.color.white)
                            isFollow = false
                        }
                } else {
                    Firebase.firestore.collection(USER_NODE).document(uid).get()
                        .addOnSuccessListener {
                            val user: User = it.toObject<User>()!!
                            Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW)
                                .document()
                                .set(user).addOnSuccessListener {
                                    binding.btnFollow.text = "Following"
                                    changeFollowersCount(1,uid)
                                    changeFollowingCount(1)

                                    val currentCountText = binding.followersCount.text.toString()
                                    val currentCount = currentCountText.toIntOrNull() ?: 0
                                    val newCount = currentCount + 1
                                    binding.followersCount.text = newCount.toString()

                                    binding.btnMessage.visibility = View.VISIBLE
                                    binding.btnFollow.backgroundTintList =
                                        ContextCompat.getColorStateList(this, R.color.gray)
                                    isFollow = true
                                }
                        }
                }
            }

            binding.btnMessage.setOnClickListener {
                val intent = Intent(this@OthersProfileActivity, DetailedChatActivity::class.java)
                intent.putExtra("userId", uid)
                intent.putExtra("userName", userName)
                startActivity(intent)
                finish()
            }
        }
    }
}