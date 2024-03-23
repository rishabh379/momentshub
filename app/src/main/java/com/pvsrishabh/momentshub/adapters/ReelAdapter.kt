package com.pvsrishabh.momentshub.adapters

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.Models.Comment
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.databinding.ReelDesignBinding
import com.pvsrishabh.momentshub.models.Reel
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.ui.OthersProfileActivity
import com.pvsrishabh.momentshub.utils.COMMENT
import com.pvsrishabh.momentshub.utils.LIKE
import com.pvsrishabh.momentshub.utils.REEL
import com.pvsrishabh.momentshub.utils.REELLIKE
import com.pvsrishabh.momentshub.utils.USER_NODE
import com.squareup.picasso.Picasso

class ReelAdapter(var context: Context, var reelList: ArrayList<Reel>) :
    RecyclerView.Adapter<ReelAdapter.ViewHolder>() {
    inner class ViewHolder(var binding: ReelDesignBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ReelDesignBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return reelList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid)
            .get().addOnSuccessListener {
                currentUSER = it.toObject<User>()!!
            }

        Picasso.get().load(reelList.get(position).profileLink).placeholder(R.drawable.user)
            .into(holder.binding.profile)
        holder.binding.caption.setText(reelList.get(position).caption)
        holder.binding.videoView.setVideoPath(reelList.get(position).videoUrl)
        holder.binding.videoView.setOnPreparedListener { mp ->
            holder.binding.progressBar.visibility = View.GONE
            mp.isLooping = true // Set the video to loop
            holder.binding.videoView.start()
        }
        // Set on completion listener to restart video playback
        holder.binding.videoView.setOnCompletionListener { mp ->
            mp.start()
        }

        if (reelList[position].likes < 0) {
            holder.binding.likesCount.text = "0"
        } else {
            holder.binding.likesCount.text = reelList[position].likes.toString()
        }

        holder.binding.commentsCount.text = (reelList[position].comments ?: 0).toString()

        val currUserId = Firebase.auth.currentUser!!.uid
        if (!reelList[position].uid.isNullOrEmpty()) {
            if (reelList[position].uid != currUserId)
                holder.binding.profile.setOnClickListener {
                    val intent = Intent(context, OthersProfileActivity::class.java)
                    intent.putExtra("uid", reelList[position].uid)
                    context.startActivity(intent)
                }
        }

        holder.binding.share.setOnClickListener {
            val i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            i.putExtra(Intent.EXTRA_TEXT, reelList[position].videoUrl)
            context.startActivity(i)
        }

        val db = Firebase.firestore
        var like = 0

        db.collection(currUserId + REELLIKE)
            .document(reelList[position].docId!!)
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
                Toast.makeText(context, "An Error occured", Toast.LENGTH_SHORT)
                    .show()
            }

        holder.binding.like.setOnClickListener {
            if (like == 0) {
                try {
                    holder.binding.like.setImageResource(R.drawable.heart)
                    db.collection(currUserId + REELLIKE)
                        .document(reelList[position].docId!!).set(reelList[position])
                        .addOnSuccessListener {
                            // increase likes count
                            val reelDocRef =
                                Firebase.firestore.collection(REEL)
                                    .document(reelList[position].docId!!)

                            Firebase.firestore.runTransaction { transaction ->
                                val snapshot = transaction.get(reelDocRef)
                                val likes = snapshot.getLong("likes") ?: 0
                                transaction.update(reelDocRef, "likes", likes + 1)
                            }.addOnSuccessListener {
                                // Update successful
                                like = 1
                                val nlikesCount: Long =
                                    holder.binding.likesCount.text.toString().toLong() + 1
                                holder.binding.likesCount.text = "${nlikesCount}"
                            }.addOnFailureListener {
                                Toast.makeText(context, "An Error occured", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    Toast.makeText(context, "An Error occured", Toast.LENGTH_SHORT)
                        .show()

                }
            } else {
                try {
                    holder.binding.like.setImageResource(R.drawable.like)
                    db.collection(currUserId + LIKE)
                        .document(reelList[position].docId!!).delete().addOnSuccessListener {
                            // decrease likes count
                            val reelDocRef =
                                Firebase.firestore.collection(REEL)
                                    .document(reelList[position].docId!!)

                            Firebase.firestore.runTransaction { transaction ->
                                val snapshot = transaction.get(reelDocRef)
                                var likesC = snapshot.getLong("likes") ?: 1
                                if (likesC < 1) {
                                    likesC = 1
                                }
                                transaction.update(reelDocRef, "likes", likesC - 1)
                            }.addOnSuccessListener {
                                like = 0
                                var likesCount: Long =
                                    holder.binding.likesCount.text.toString().toLong()
                                if (likesCount.toInt() <= 0) {
                                    likesCount = 1
                                }
                                val nlikesCount = likesCount - 1
                                holder.binding.likesCount.text = "${nlikesCount}"
                            }.addOnFailureListener { e ->
                                // Handle any errors
                            }
                        }.addOnFailureListener {
                            Toast.makeText(context, "An Error occured", Toast.LENGTH_SHORT)
                                .show()
                        }
                } catch (e: Exception) {
                    Toast.makeText(context, "An Error occured", Toast.LENGTH_SHORT)
                        .show()

                }
            }
        }
        holder.binding.comment.setOnClickListener {
            try {
                requestManagerForReel = Glide.with(context)
                val path = reelList[position].docId + REEL + COMMENT
                val query =
                    Firebase.firestore.collection(path).orderBy("time", Query.Direction.DESCENDING)

                val registration = query.addSnapshotListener { value, error ->
                    if (error != null) {
                        // Handle error
                        return@addSnapshotListener
                    }
                    val tempList = ArrayList<Comment>()
                    commentList.clear()
                    for (document in value!!.documents) {
                        val comment = document.toObject<Comment>()!!
                        comment.docId = reelList[position].docId
                        tempList.add(comment)
                    }
                    commentList.clear()
                    commentList.addAll(tempList)
                    commentAdapter.notifyDataSetChanged()
                }

                commentAdapter = CommentAdapter(context, commentList, requestManagerForReel)
                try {
                    showDialog(path, position, holder)
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Error Occurred while loading Comments",
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

    private fun showDialog(path: String, position: Int, holder: ViewHolder) {
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
                model.isReel = true
                editTextMsg.setText("")

                Firebase.firestore.collection(path).document(model.uid + model.time).set(model)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Comment Added", Toast.LENGTH_SHORT).show()
                        Firebase.firestore.collection(REEL).document(reelList[position].docId!!)
                            .update("comments", FieldValue.increment(1)).addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Comments count update failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }.addOnFailureListener {
                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.setOnDismissListener {
                try {
                    Firebase.firestore.collection(REEL).document(reelList[position].docId!!).get()
                        .addOnSuccessListener {
                            val tempReel = it.toObject<Reel>()!!
                            holder.binding.commentsCount.text = (tempReel.comments ?: 0).toString()
                        }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error Occured", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.show()
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
            dialog.window?.setGravity(Gravity.BOTTOM)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error displaying dialog", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var commentAdapter: CommentAdapter
    private var commentList = ArrayList<Comment>()
    private lateinit var requestManagerForReel: RequestManager
}