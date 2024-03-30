package com.pvsrishabh.momentshub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pvsrishabh.momentshub.databinding.MyPostRvDesignBinding
import com.pvsrishabh.momentshub.models.Post
import com.squareup.picasso.Picasso

class MyPostRvAdapter(var context: Context, var postList: ArrayList<Post>, private val callback: AdapterCallback) :
    RecyclerView.Adapter<MyPostRvAdapter.ViewHolder>() {

    interface AdapterCallback {
        fun onItemLongClicked(position: Int, post: Post)
    }

    inner class ViewHolder(var binding: MyPostRvDesignBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MyPostRvDesignBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = postList[position]
        Picasso.get().load(post.postUrl).into(holder.binding.postImage)

        holder.itemView.setOnLongClickListener {
            callback.onItemLongClicked(position, post)
            false
        }
    }
}