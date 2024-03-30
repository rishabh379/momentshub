package com.pvsrishabh.momentshub.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.databinding.SearchRvBinding
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.ui.OthersProfileActivity
import com.pvsrishabh.momentshub.utils.FOLLOW
import com.pvsrishabh.momentshub.utils.FOLLOWERS
import com.pvsrishabh.momentshub.utils.USER_NODE
import com.pvsrishabh.momentshub.utils.changeFollowersCount
import com.pvsrishabh.momentshub.utils.changeFollowingCount

class SearchAdapter(
    var context: Context,
    var userList: ArrayList<User>,
    private val errorHandlingListener: ErrorHandlingListener
) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    interface ErrorHandlingListener {
        fun handleErrorAndNavigate()
    }

    inner class ViewHolder(var binding: SearchRvBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding = SearchRvBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            var isFollow = false

            Glide.with(context.applicationContext).load(userList[position].image)
                .placeholder(R.drawable.user_icon)
                .into(holder.binding.profileImage)

            holder.binding.name.text = userList[position].name

            val currUserId = Firebase.auth.currentUser!!.uid

            if (userList[position].userId == currUserId) {
                holder.binding.follow.visibility = View.GONE
            }

            Firebase.firestore.collection(currUserId + FOLLOW)
                .whereEqualTo("email", userList[position].email).get().addOnSuccessListener {
                    if (it.documents.size != 0) {
                        holder.binding.follow.text = "Unfollow"
                        holder.binding.follow.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.gray)
                        isFollow = true
                    } else {
                        isFollow = false
                    }
                }

            holder.binding.name.setOnClickListener {
                if (userList.isNotEmpty() && position < userList.size) {
                    if (userList[position].userId != currUserId) {
                        val intent = Intent(context, OthersProfileActivity::class.java)
                        intent.putExtra("uid", userList[position].userId)
                        context.startActivity(intent)
                    }
                }
            }

            var currUser: User? = null
            Firebase.firestore.collection(USER_NODE).document(currUserId).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        currUser = it.toObject<User>()
                    }
                }

            if (userList.isNotEmpty() && position in userList.indices && userList.size != 0) {
                holder.binding.follow.setOnClickListener {
                    holder.binding.follow.isEnabled = false
                    if (isFollow) {
                        try {
                            Firebase.firestore.collection(currUserId + FOLLOW)
                                .whereEqualTo("userId", userList[position].userId)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    if (!querySnapshot.isEmpty) {
                                        val followDocId = querySnapshot.documents[0].id

                                        // Delete the follow document in currUserId + FOLLOW collection
                                        try {
                                            val deleteFollowTask =
                                                Firebase.firestore.collection(currUserId + FOLLOW)
                                                    .document(followDocId)
                                                    .delete()

                                            // Find and delete the corresponding follower document in userList[position].userId + FOLLOWERS collection
                                            val deleteFollowerTask =
                                                Firebase.firestore.collection(userList[position].userId + FOLLOWERS)
                                                    .whereEqualTo("userId", currUser!!.userId)
                                                    .get()
                                                    .addOnSuccessListener { followerQuerySnapshot ->
                                                        if (!followerQuerySnapshot.isEmpty) {
                                                            val followerDocId =
                                                                followerQuerySnapshot.documents[0].id

                                                            Firebase.firestore.collection(userList[position].userId + FOLLOWERS)
                                                                .document(followerDocId)
                                                                .delete()
                                                        }
                                                    }

                                            // Execute both delete tasks concurrently
                                            Tasks.whenAllComplete(
                                                deleteFollowTask,
                                                deleteFollowerTask
                                            )
                                        } catch (e: Exception) {
                                            // Handle the error appropriately
                                            Toast.makeText(
                                                context,
                                                "An error occurred",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            errorHandlingListener.handleErrorAndNavigate()
                                        }
                                    }

                                    holder.binding.follow.text = "Follow"

                                    changeFollowingCount(-1)
                                    changeFollowersCount(-1, userList[position].userId!!)
                                    holder.binding.follow.backgroundTintList =
                                        ContextCompat.getColorStateList(context, R.color.blue)
                                    isFollow = false
                                    holder.binding.follow.isEnabled = true
                                }
                        } catch (e: Exception) {
                            // Handle the error appropriately
                            Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show()
                            errorHandlingListener.handleErrorAndNavigate()
                        }
                    } else {
                        try {
                            val followCollection =
                                Firebase.firestore.collection(currUserId + FOLLOW)
                            val followerCollection =
                                Firebase.firestore.collection(userList[position].userId!! + FOLLOWERS)

                            val followDocument = followCollection.document()
                            val followerDocument = followerCollection.document()

                            val batch = Firebase.firestore.batch()

                            batch.set(followDocument, userList[position])
                            batch.set(followerDocument, currUser!!)

                            batch.commit()
                                .addOnSuccessListener {
                                    // Update UI and handle success
                                    holder.binding.follow.text = "Unfollow"
                                    holder.binding.follow.backgroundTintList =
                                        ContextCompat.getColorStateList(context, R.color.gray)

                                    // Increase followers count of the user
                                    changeFollowersCount(1, userList[position].userId!!)
                                    changeFollowingCount(1)

                                    isFollow = true
                                    holder.binding.follow.isEnabled = true
                                }
                                .addOnFailureListener { exception ->
                                    // Handle failure
                                }

                        } catch (e: Exception) {
                            // Handle the error appropriately
                            Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show()
                            errorHandlingListener.handleErrorAndNavigate()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Handle any exceptions here
            Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show()
            errorHandlingListener.handleErrorAndNavigate()
        }
    }
}