package com.pvsrishabh.momentshub.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.databinding.SearchRvBinding
import com.pvsrishabh.momentshub.fragments.ProfileFragment
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.ui.HomeActivity
import com.pvsrishabh.momentshub.ui.OthersProfileActivity
import com.pvsrishabh.momentshub.utils.FOLLOW
import com.pvsrishabh.momentshub.utils.FOLLOWERS
import com.pvsrishabh.momentshub.utils.USER_NODE
import com.pvsrishabh.momentshub.utils.changeFollowersCount
import com.pvsrishabh.momentshub.utils.changeFollowingCount

class SearchAdapter(var context: Context, var userList: ArrayList<User>) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

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
        var isFollow = false

        Glide.with(context).load(userList[position].image).placeholder(R.drawable.user_icon)
            .into(holder.binding.profileImage)

        holder.binding.name.text = userList[position].name

        val currUserId = Firebase.auth.currentUser!!.uid

        if(userList[position].userId == currUserId){
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
                if(userList[position].userId != currUserId){
                    val intent = Intent(context, OthersProfileActivity::class.java)
                    intent.putExtra("uid", userList[position].userId)
                    context.startActivity(intent)
                }
            }
        }

        var currUser: User? = null
        Firebase.firestore.collection(USER_NODE).document(currUserId).get()
            .addOnSuccessListener {
                if(it.exists()){
                    currUser = it.toObject<User>()
                }
            }

        holder.binding.follow.setOnClickListener {
            if (isFollow) {
                Firebase.firestore.collection(currUserId + FOLLOW)
                    .whereEqualTo("email", userList[position].email).get()
                    .addOnSuccessListener {
                        if (!it.isEmpty) {
                            Firebase.firestore.collection(currUserId + FOLLOW)
                                .document(it.documents[0].id).delete().addOnSuccessListener {
                                    Firebase.firestore.collection(userList[position].userId + FOLLOWERS)
                                        .whereEqualTo("email", currUser!!.email).get()
                                        .addOnSuccessListener {docs->
                                            if (!docs.isEmpty) {
                                                Firebase.firestore.collection(userList[position].userId + FOLLOWERS)
                                                    .document(docs.documents[0].id).delete()
                                            }
                                        }
                                }
                        }

                        holder.binding.follow.text = "Follow"

                        changeFollowingCount(-1)
                        changeFollowersCount(-1, userList[position].userId!!)
                        holder.binding.follow.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.blue)
                        isFollow = false
                    }
            } else {
                Firebase.firestore.collection(currUserId + FOLLOW)
                    .document()
                    .set(userList[position]).addOnSuccessListener {
                        holder.binding.follow.text = "Unfollow"

                        Firebase.firestore.collection(userList[position].userId!! + FOLLOWERS)
                            .document().set(currUser!!)

                        // increase followers count of the user
                        changeFollowersCount(1, userList[position].userId!!)
                        changeFollowingCount(1)

                        holder.binding.follow.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.gray)
                        isFollow = true
                    }
            }

        }
    }
}
