package com.pvsrishabh.momentshub.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
import com.pvsrishabh.momentshub.utils.FOLLOWERS
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
            if (intent.getStringExtra("uid") != Firebase.auth.currentUser!!.uid) {
                val uid = intent.getStringExtra("uid")
                if (!uid.isNullOrEmpty()) {

                    var userName: String? = ""

                    viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, uid)
                    viewPagerAdapter.addFragment(MyPostFragment(), "Posts")
                    viewPagerAdapter.addFragment(MyReelsFragment(), "Reels")
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
                            binding.followingCount.text = (user.followingCount ?: 0).toString()
                            binding.postCount.text = (user.postCount ?: 0).toString()
                            binding.followersCount.text = (user.followersCount ?: 0).toString()
                            if (!user.image.isNullOrEmpty()) {
                                Picasso.get().load(user.image).into(binding.profileImage)
                            }
                        }.addOnFailureListener {
                            Toast.makeText(
                                this@OthersProfileActivity,
                                "Failed",
                                Toast.LENGTH_SHORT
                            ).show()
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
                        }.addOnFailureListener {
                            Toast.makeText(
                                this@OthersProfileActivity,
                                "Failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    var currUser: User? = null
                    Firebase.firestore.collection(USER_NODE)
                        .document(Firebase.auth.currentUser!!.uid)
                        .get()
                        .addOnSuccessListener {
                            currUser = it.toObject<User>()
                        }.addOnFailureListener {
                            Toast.makeText(
                                this@OthersProfileActivity,
                                "Failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    binding.followersLl.setOnClickListener {
                        if (uid.isNotEmpty()) {
                            val intent =
                                Intent(this@OthersProfileActivity, FollowActivity::class.java)
                            intent.putExtra("uid", uid)
                            intent.putExtra("type", FOLLOWERS)
                            startActivity(intent)
                        }
                    }

                    binding.followingLl.setOnClickListener {
                        if (uid.isNotEmpty()) {
                            val intent =
                                Intent(this@OthersProfileActivity, FollowActivity::class.java)
                            intent.putExtra("uid", uid)
                            intent.putExtra("type", FOLLOW)
                            startActivity(intent)
                        }
                    }

                    binding.btnFollow.setOnClickListener {
                        if (isFollow) {
                            val currentUser = Firebase.auth.currentUser
                            val followCollection =
                                Firebase.firestore.collection(currentUser!!.uid + FOLLOW)

                            val followQueryTask = followCollection.whereEqualTo("userId", uid).get()

                            followQueryTask.addOnSuccessListener { querySnapshot ->
                                if (!querySnapshot.isEmpty) {
                                    val deleteFollowTask =
                                        followCollection.document(querySnapshot.documents[0].id)
                                            .delete()

                                    deleteFollowTask.addOnSuccessListener {
                                        val followerCollection =
                                            Firebase.firestore.collection(uid + FOLLOWERS)
                                        val followerQueryTask = followerCollection.whereEqualTo(
                                            "email",
                                            currentUser.email
                                        ).get()

                                        followerQueryTask.addOnSuccessListener { followerQuerySnapshot ->
                                            if (!followerQuerySnapshot.isEmpty) {
                                                val followerDocumentId =
                                                    followerQuerySnapshot.documents[0].id
                                                val deleteFollowerTask =
                                                    followerCollection.document(followerDocumentId)
                                                        .delete()

                                                deleteFollowerTask.addOnFailureListener {
                                                    showToast()
                                                }
                                            }
                                        }.addOnFailureListener {
                                            showToast()
                                        }
                                    }.addOnFailureListener {
                                        showToast()
                                    }
                                }
                            }.addOnFailureListener {
                                showToast()
                            }

                            binding.btnFollow.text = "Follow"
                            val currentCountText = binding.followersCount.text.toString()
                            val currentCount = currentCountText.toIntOrNull() ?: 0
                            var newCount = currentCount - 1
                            if (newCount < 0) {
                                newCount = 0
                            }
                            binding.followersCount.text = newCount.toString()

                            changeFollowersCount(-1, uid)
                            changeFollowingCount(-1)
                            binding.btnMessage.visibility = View.GONE
                            binding.btnFollow.backgroundTintList =
                                ContextCompat.getColorStateList(this, R.color.white)
                            isFollow = false

                        } else {
                            Firebase.firestore.collection(USER_NODE).document(uid).get()
                                .addOnSuccessListener { userDocument ->
                                    val user: User =
                                        userDocument.toObject<User>() ?: return@addOnSuccessListener

                                    val currentUserUid = Firebase.auth.currentUser!!.uid
                                    val followCollection =
                                        Firebase.firestore.collection(currentUserUid + FOLLOW)

                                    val followTask = followCollection.document().set(user)

                                    followTask.addOnSuccessListener {
                                        binding.btnFollow.text = "Following"

                                        val followerCollection =
                                            Firebase.firestore.collection(uid + FOLLOWERS)
                                        val followerTask =
                                            followerCollection.document().set(currUser!!)

                                        followerTask.addOnSuccessListener {
                                            changeFollowersCount(1, uid)

                                            val currentCountText =
                                                binding.followersCount.text.toString()
                                            val currentCount = currentCountText.toIntOrNull() ?: 0
                                            val newCount = currentCount + 1
                                            binding.followersCount.text = newCount.toString()

                                            binding.btnMessage.visibility = View.VISIBLE
                                            binding.btnFollow.backgroundTintList =
                                                ContextCompat.getColorStateList(this, R.color.gray)
                                            isFollow = true
                                        }.addOnFailureListener {
                                            showToast()
                                        }

                                        changeFollowingCount(1)
                                    }.addOnFailureListener {
                                        showToast()
                                    }
                                }
                                .addOnFailureListener {
                                    showToast()
                                }
                        }
                    }
                    binding.btnMessage.setOnClickListener {
                        val intent =
                            Intent(this@OthersProfileActivity, DetailedChatActivity::class.java)
                        intent.putExtra("userId", uid)
                        intent.putExtra("userName", userName)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun showToast() {
        Toast.makeText(this@OthersProfileActivity, "Failed", Toast.LENGTH_SHORT).show()
    }
}