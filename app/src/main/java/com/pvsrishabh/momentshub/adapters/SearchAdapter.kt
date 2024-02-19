package com.pvsrishabh.momentshub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.pvsrishabh.momentshub.Models.User
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.Utils.FOLLOW
import com.pvsrishabh.momentshub.databinding.SearchRvBinding

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

        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW)
            .whereEqualTo("email", userList[position].email).get().addOnSuccessListener {
                if (it.documents.size != 0) {
                    holder.binding.follow.text = "Unfollow"
                    holder.binding.follow.backgroundTintList=
                        ContextCompat.getColorStateList(context, R.color.gray)
                    isFollow = true
                } else {
                    isFollow = false
                }
            }

        holder.binding.follow.setOnClickListener {
            if (isFollow) {
                Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW)
                    .whereEqualTo("email", userList[position].email).get().addOnSuccessListener {

                        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW)
                            .document(it.documents[0].id).delete()
                        holder.binding.follow.text = "Follow"
                        holder.binding.follow.backgroundTintList=
                            ContextCompat.getColorStateList(context, R.color.blue)
                        isFollow=false
                    }
            } else {
                Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW).document()
                    .set(userList[position]).addOnSuccessListener {
                        holder.binding.follow.text = "Unfollow"
                        holder.binding.follow.backgroundTintList=
                            ContextCompat.getColorStateList(context, R.color.gray)
                        isFollow=true
                    }
            }

        }
    }
}