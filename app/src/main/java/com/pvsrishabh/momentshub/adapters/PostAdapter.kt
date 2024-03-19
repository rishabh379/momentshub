package com.pvsrishabh.momentshub.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.databinding.PostRvBinding
import com.pvsrishabh.momentshub.models.Post
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.utils.LIKE
import com.pvsrishabh.momentshub.utils.SAVE
import com.pvsrishabh.momentshub.utils.USER_NODE

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

    override fun onBindViewHolder(holder: myHolder, position: Int) {

        val db = Firebase.firestore

        db.collection(USER_NODE).document(postList[position].uid).get()
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

        var like = 0
        var save = 0

        db.collection(Firebase.auth.currentUser!!.uid + LIKE).document(postList[position].docId!!)
            .get()
            .addOnSuccessListener {
                if (!it.exists()) {
                    like = 0
                    holder.binding.like.setImageResource(R.drawable.like)
                } else {
                    like = 1
                    holder.binding.like.setImageResource(R.drawable.heart)
                }
            }

        db.collection(Firebase.auth.currentUser!!.uid + SAVE).document(postList[position].docId!!)
            .get()
            .addOnSuccessListener {
                if (!it.exists()) {
                    save = 0
                    holder.binding.save.setImageResource(R.drawable.bookmark)
                } else {
                    save = 1
                    holder.binding.save.setImageResource(R.drawable.filled_bookmark)
                }
            }

        holder.binding.like.setOnClickListener {
            if (like == 0) {
                holder.binding.like.setImageResource(R.drawable.heart)
                db.collection(Firebase.auth.currentUser!!.uid + LIKE)
                    .document(postList[position].docId!!).set(postList[position])
                    .addOnSuccessListener {
                        like = 1
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
            } else {
                holder.binding.like.setImageResource(R.drawable.like)
                db.collection(Firebase.auth.currentUser!!.uid + LIKE)
                    .document(postList[position].docId!!).delete().addOnSuccessListener {
                    like = 0
                }
            }
        }

        holder.binding.save.setOnClickListener {
            if (save == 0) {
                holder.binding.save.setImageResource(R.drawable.filled_bookmark)
                db.collection(Firebase.auth.currentUser!!.uid + SAVE)
                    .document(postList[position].docId!!).set(postList[position])
                    .addOnSuccessListener {
                        save = 1
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
            } else {
                holder.binding.save.setImageResource(R.drawable.bookmark)
                db.collection(Firebase.auth.currentUser!!.uid + SAVE)
                    .document(postList[position].docId!!).delete().addOnSuccessListener {
                    save = 0
                }
            }
        }


        holder.binding.share.setOnClickListener {
            val i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            i.putExtra(Intent.EXTRA_TEXT, postList[position].postUrl)
            context.startActivity(i)
        }
    }
}