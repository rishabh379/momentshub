package com.pvsrishabh.momentshub.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.models.Post
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.utils.USER_NODE
import com.pvsrishabh.momentshub.databinding.PostRvBinding

class PostAdapter(var context: Context, var postList: ArrayList<Post>) :
    RecyclerView.Adapter<PostAdapter.myHolder>() {

    inner class myHolder(var binding: PostRvBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myHolder {
        val binding = PostRvBinding.inflate(LayoutInflater.from(context), parent, false)
        return myHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    //my work strt
    private val drawableResourcesLike = arrayOf(
        R.drawable.like,
        R.drawable.heart
    )

    private val drawableResourcesSave = arrayOf(
        R.drawable.bookmark,
        R.drawable.filled_bookmark
    )

    private var currentDrawableIndexLike = 0

    private fun changeImageResourceLike(imageView: ImageView) {
        currentDrawableIndexLike = (currentDrawableIndexLike + 1) % drawableResourcesLike.size
        imageView.setImageResource(drawableResourcesLike[currentDrawableIndexLike])
    }

    private var currentDrawableIndexSave = 0

    private fun changeImageResourceSave(imageView: ImageView) {
        currentDrawableIndexSave = (currentDrawableIndexSave + 1) % drawableResourcesSave.size
        imageView.setImageResource(drawableResourcesSave[currentDrawableIndexSave])
    }
    // ends

    override fun onBindViewHolder(holder: myHolder, position: Int) {
        Firebase.firestore.collection(USER_NODE).document(postList[position].uid).get()
            .addOnSuccessListener {
                val user = it.toObject<User>()
                Glide.with(context).load(user!!.image).placeholder(R.drawable.user_icon)
                    .into(holder.binding.profileImage)
                holder.binding.tvName.text = user.name
            }
        Glide.with(context).load(postList[position].postUrl).placeholder(R.drawable.loading)
            .into(holder.binding.postImage)
        holder.binding.postCaption.text = postList[position].caption

        try {
            holder.binding.time.text = TimeAgo.using(postList[position].time.toLong())
        } catch (e: Exception) {
            holder.binding.time.text = ""
        }

        holder.binding.like.setOnClickListener {
            changeImageResourceLike(holder.binding.like)
        }

        holder.binding.save.setOnClickListener {
            changeImageResourceSave(holder.binding.save)
        }

        holder.binding.share.setOnClickListener {
            var i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            i.putExtra(Intent.EXTRA_TEXT, postList[position].postUrl)
            context.startActivity(i)
        }
    }
}