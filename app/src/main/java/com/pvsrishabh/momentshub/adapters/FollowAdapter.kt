package com.pvsrishabh.momentshub.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.databinding.StoryRvDesignBinding
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.ui.StoryActivity
import com.pvsrishabh.momentshub.utils.extractAndTrimFirstString

class FollowAdapter(var context: Context, var followList: ArrayList<User>) :
    RecyclerView.Adapter<FollowAdapter.ViewHolder>() {
    inner class ViewHolder(var binding: StoryRvDesignBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = StoryRvDesignBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return followList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(followList[position].image).placeholder(R.drawable.user_icon)
            .into(holder.binding.profileImage)
        holder.binding.name.text = extractAndTrimFirstString(followList[position].name.toString())
        holder.binding.profileImage.setOnClickListener {
            val intent = Intent(context, StoryActivity::class.java)
            intent.putExtra("uid", followList[position].userId)
            context.startActivity(intent)
        }
    }
}