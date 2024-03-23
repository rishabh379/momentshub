package com.pvsrishabh.momentshub.adapters

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.Models.Comment
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.databinding.PostRvBinding
import com.pvsrishabh.momentshub.models.Post
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.ui.HomeActivity
import com.pvsrishabh.momentshub.ui.OthersProfileActivity
import com.pvsrishabh.momentshub.utils.COMMENT
import com.pvsrishabh.momentshub.utils.LIKE
import com.pvsrishabh.momentshub.utils.POST
import com.pvsrishabh.momentshub.utils.SAVE
import com.pvsrishabh.momentshub.utils.USER_NODE
import com.pvsrishabh.momentshub.utils.extractAndTrimFirstString

class PostAdapter(
    var context: Context,
    var postList: ArrayList<Post>,
    private val requestManager: RequestManager
) :
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

        Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid)
            .get().addOnSuccessListener {
                currentUSER = it.toObject<User>()!!
            }

        try {

            val db = Firebase.firestore

            db.collection(USER_NODE).document(postList[position].uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    val user = documentSnapshot.toObject<User>()!!
                    user.let {
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
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to Load Post's Like", Toast.LENGTH_SHORT).show()
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
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to Load Post's Save", Toast.LENGTH_SHORT).show()
                }

            holder.binding.like.setOnClickListener {
                if (like == 0) {
                    try {
                        holder.binding.like.setImageResource(R.drawable.heart)

                        db.collection(Firebase.auth.currentUser!!.uid + LIKE)
                            .document(postList[position].docId!!).set(postList[position])
                            .addOnSuccessListener {
                                // increase likes count
                                val postDocRef =
                                    Firebase.firestore.collection(POST)
                                        .document(postList[position].docId!!)
                                postDocRef.update("likes", FieldValue.increment(1))
                                    .addOnSuccessListener {
                                        Log.d(TAG, "Likes count updated successfully!")
                                        like = 1
                                        Toast.makeText(context, "Liked", Toast.LENGTH_SHORT).show()

                                        val nlikesCount: Long =
                                            extractAndTrimFirstString(holder.binding.likesCount.text.toString()).toLong() + 1
                                        holder.binding.likesCount.text = "${nlikesCount} likes"
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(TAG, "Error updating likes count", e)
                                    }
                            }.addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "Error updating likes count",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } catch (e: Exception) {
                        Toast.makeText(context, "An Error occured", Toast.LENGTH_SHORT)
                            .show()

                    }

                } else {
                    try {
                        holder.binding.like.setImageResource(R.drawable.like)
                        db.collection(Firebase.auth.currentUser!!.uid + LIKE)
                            .document(postList[position].docId!!).delete().addOnSuccessListener {
                                // decrease likes count
                                val postDocRef =
                                    Firebase.firestore.collection(POST)
                                        .document(postList[position].docId!!)

                                postDocRef.get().addOnSuccessListener { documentSnapshot ->
                                    val likes = documentSnapshot.getLong("likes") ?: 0
                                    if (likes > 0) {
                                        postDocRef.update("likes", FieldValue.increment(-1))
                                            .addOnSuccessListener {
                                                Log.d(TAG, "Likes count decremented successfully!")
                                                like = 0

                                                var likesCount: Long =
                                                    extractAndTrimFirstString(holder.binding.likesCount.text.toString()).toLong()
                                                if (likesCount.toInt() <= 0) {
                                                    likesCount = 1
                                                }
                                                val nlikesCount = likesCount - 1
                                                holder.binding.likesCount.text =
                                                    "$nlikesCount likes"
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(TAG, "Error decrementing likes count", e)
                                            }
                                    } else {
                                        Log.d(
                                            TAG,
                                            "Likes count is already 0, no need to decrement further."
                                        )
                                    }
                                }.addOnFailureListener { e ->
                                    Log.e(TAG, "Error getting document", e)
                                }
                            }
                    } catch (e: Exception) {
                        Toast.makeText(context, "An Error occured", Toast.LENGTH_SHORT)
                            .show()

                    }
                }
            }

            holder.binding.save.setOnClickListener {
                if (save == 0) {
                    try {
                        db.collection(Firebase.auth.currentUser!!.uid + SAVE)
                            .document(postList[position].docId!!).set(postList[position])
                            .addOnSuccessListener {
                                save = 1
                                holder.binding.save.setImageResource(R.drawable.filled_bookmark)

                                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT)
                                    .show()
                            }
                    } catch (e: Exception) {
                        Toast.makeText(context, "An Error occured", Toast.LENGTH_SHORT)
                            .show()

                    }
                } else {
                    try {
                        db.collection(Firebase.auth.currentUser!!.uid + SAVE)
                            .document(postList[position].docId!!).delete().addOnSuccessListener {
                                save = 0
                                holder.binding.save.setImageResource(R.drawable.bookmark)
                                Toast.makeText(context, "Unsaved", Toast.LENGTH_SHORT).show()
                            }
                    } catch (e: Exception) {
                        Toast.makeText(context, "An Error occured", Toast.LENGTH_SHORT)
                            .show()
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
        } catch (e: Exception) {
            // Handle any exceptions here
            Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show()
            context.startActivity(Intent(context, HomeActivity::class.java))
        }

        holder.binding.comment.setOnClickListener {
            try {
                requestManagerForComment = Glide.with(context)
                val path = postList[position].docId + POST + COMMENT
                try {
                    val query =
                        Firebase.firestore.collection(path)
                            .orderBy("time", Query.Direction.DESCENDING)

                    val registration = query.addSnapshotListener { value, error ->

                        if (error != null) {
                            // Handle error
                            return@addSnapshotListener
                        }
                        val tempList = ArrayList<Comment>()
                        commentList.clear()
                        for (document in value!!.documents) {
                            val comment = document.toObject<Comment>()!!
                            comment.docId = postList[position].docId
                            tempList.add(comment)
                        }
                        commentList.clear()
                        commentList.addAll(tempList)
                        commentAdapter.notifyDataSetChanged()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error Occurred", Toast.LENGTH_SHORT).show()
                }

                commentAdapter = CommentAdapter(context, commentList, requestManagerForComment)
                try {
                    showDialog(path)
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Error Occurred while loading comments",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error Occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private lateinit var currentUSER: User
    }

    private fun showDialog(path: String) {
        try {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.bottom_sheet_layout)
            val commentRv = dialog.findViewById<RecyclerView>(R.id.rv_comment)
            val editTextMsg = dialog.findViewById<EditText>(R.id.editTextMsg)
            val send = dialog.findViewById<ImageView>(R.id.send)

            commentRv.layoutManager = LinearLayoutManager(context)
            commentRv.adapter = commentAdapter

            send.setOnClickListener {
                if (editTextMsg.text.toString().isEmpty()) {
                    editTextMsg.error = "Enter Your Message"
                    return@setOnClickListener
                }
                val message = editTextMsg.text.toString()
                val model = Comment(message, System.currentTimeMillis().toString())
                model.userProfile = currentUSER.image
                model.uid = currentUSER.userId!!
                model.userName = currentUSER.name!!
                editTextMsg.setText("")

                Firebase.firestore.collection(path).document(model.uid + model.time).set(model)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Comment Added", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                    }
            }

            dialog.show()
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
            dialog.window?.setGravity(Gravity.BOTTOM)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error displaying dialog", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var commentAdapter: CommentAdapter
    private var commentList = ArrayList<Comment>()
    private lateinit var requestManagerForComment: RequestManager
}