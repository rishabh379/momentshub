package com.pvsrishabh.momentshub.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.pvsrishabh.momentshub.Models.User
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.databinding.MsgRvDesignBinding
import com.pvsrishabh.momentshub.ui.DetailedChatActivity

class MessageAdapter(var context: Context, var userList: ArrayList<User>) :

    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    inner class ViewHolder(var binding: MsgRvDesignBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MsgRvDesignBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Glide.with(context).load(userList[position].image).placeholder(R.drawable.user_icon)
            .into(holder.binding.profileImage)

        holder.binding.name.text = userList[position].name

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailedChatActivity::class.java)
            intent.putExtra("userId", userList[position].userId)
            intent.putExtra("userName", userList[position].name)
            holder.itemView.context.startActivity(intent)
            (context as Activity).finish()
        }
    }
}