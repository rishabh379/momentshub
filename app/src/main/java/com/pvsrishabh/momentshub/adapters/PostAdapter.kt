package com.pvsrishabh.momentshub.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.databinding.PostRvBinding
import com.pvsrishabh.momentshub.models.Post
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.ui.HomeActivity
import com.pvsrishabh.momentshub.ui.OthersProfileActivity
import com.pvsrishabh.momentshub.utils.LIKE
import com.pvsrishabh.momentshub.utils.POST
import com.pvsrishabh.momentshub.utils.SAVE
import com.pvsrishabh.momentshub.utils.USER_NODE
import com.pvsrishabh.momentshub.utils.extractAndTrimFirstString

class PostAdapter(var context: Context, var postList: ArrayList<Post>, private val requestManager: RequestManager) :
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

        try {

            val db = Firebase.firestore

            db.collection(USER_NODE).document(postList[position].uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    val user = documentSnapshot.toObject<User>()
                    user?.let {
                        requestManager.load(user.image)
                            .placeholder(R.drawable.user_icon)
                            .into(holder.binding.profileImage)
                        holder.binding.tvName.text = user.name
                    }
                }

            requestManager.load(postList[position].postUrl)
                .placeholder(R.drawable.loading)
                .into(holder.binding.postImage)

            holder.binding.postCaption.text = postList[position].caption
            holder.binding.likesCount.text = "${postList[position].likes} likes"

            try {
                holder.binding.time.text = TimeAgo.using(postList[position].time.toLong())
            } catch (e: Exception) {
                holder.binding.time.text = ""
            }

            var like = 0
            var save = 0

            db.collection(Firebase.auth.currentUser!!.uid + LIKE)
                .document(postList[position].docId!!)
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

            db.collection(Firebase.auth.currentUser!!.uid + SAVE)
                .document(postList[position].docId!!)
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
                    val nlikesCount: Long =
                        extractAndTrimFirstString(holder.binding.likesCount.text.toString()).toLong() + 1
                    holder.binding.likesCount.text = "${nlikesCount} likes"
                    db.collection(Firebase.auth.currentUser!!.uid + LIKE)
                        .document(postList[position].docId!!).set(postList[position])
                        .addOnSuccessListener {
                            like = 1
                            Toast.makeText(context, "Liked", Toast.LENGTH_SHORT).show()
                            // increase likes count
                            val postDocRef =
                                Firebase.firestore.collection(POST)
                                    .document(postList[position].docId!!)

                            Firebase.firestore.runTransaction { transaction ->
                                val snapshot = transaction.get(postDocRef)
                                val likes = snapshot.getLong("likes") ?: 0
                                transaction.update(postDocRef, "likes", likes + 1)
                            }
                        }
                } else {
                    holder.binding.like.setImageResource(R.drawable.like)
                    var likesCount: Long =
                        extractAndTrimFirstString(holder.binding.likesCount.text.toString()).toLong()
                    if (likesCount.toInt() <= 0) {
                        likesCount = 1
                    }
                    val nlikesCount = likesCount - 1
                    holder.binding.likesCount.text = "$nlikesCount likes"
                    db.collection(Firebase.auth.currentUser!!.uid + LIKE)
                        .document(postList[position].docId!!).delete().addOnSuccessListener {
                            like = 0
                            Toast.makeText(context, "Disliked", Toast.LENGTH_SHORT).show()

                            // decrease likes count
                            val postDocRef =
                                Firebase.firestore.collection(POST)
                                    .document(postList[position].docId!!)

                            Firebase.firestore.runTransaction { transaction ->
                                val snapshot = transaction.get(postDocRef)
                                val likes = snapshot.getLong("likes") ?: 1
                                transaction.update(postDocRef, "likes", likes - 1)
                            }
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
                            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                } else {
                    holder.binding.save.setImageResource(R.drawable.bookmark)
                    db.collection(Firebase.auth.currentUser!!.uid + SAVE)
                        .document(postList[position].docId!!).delete().addOnSuccessListener {
                            save = 0
                            Toast.makeText(context, "Unsaved", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            holder.binding.share.setOnClickListener {
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plain"
                i.putExtra(Intent.EXTRA_TEXT, postList[position].postUrl)
                context.startActivity(i)
            }

            holder.binding.tvName.setOnClickListener {
                if (postList[position].uid.isNotEmpty()) {
                    if (postList[position].uid != Firebase.auth.currentUser!!.uid) {
                        val intent = Intent(context, OthersProfileActivity::class.java)
                        intent.putExtra("uid", postList[position].uid)
                        context.startActivity(intent)
                    }
                }
            }
        }catch (e: Exception) {
            // Handle any exceptions here
            Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            context.startActivity(Intent(context, HomeActivity::class.java))
        }
    }
}