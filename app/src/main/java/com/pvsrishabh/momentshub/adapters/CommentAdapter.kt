package com.pvsrishabh.momentshub.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pvsrishabh.momentshub.Models.Comment
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.databinding.CommentRvBinding
import com.pvsrishabh.momentshub.utils.COMMENT
import com.pvsrishabh.momentshub.utils.POST
import com.pvsrishabh.momentshub.utils.REEL

class CommentAdapter(
    var context: Context,
    var commentList: ArrayList<Comment>,
    private val requestManager: RequestManager
) :
    RecyclerView.Adapter<CommentAdapter.myHolder>() {

    inner class myHolder(var binding: CommentRvBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myHolder {
        val binding = CommentRvBinding.inflate(LayoutInflater.from(context), parent, false)
        return myHolder(binding)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: myHolder, position: Int) {
        try {
            val comment = commentList[position]
            holder.binding.userName.text = comment.userName
            try {
                holder.binding.time.text = TimeAgo.using(comment.time.toLong())
            } catch (e: Exception) {
                holder.binding.time.text = ""
            }
            holder.binding.commentText.text = comment.text
            requestManager.load(comment.userProfile)
                .placeholder(R.drawable.user_icon)
                .into(holder.binding.profileImg)

            if (comment.uid == Firebase.auth.currentUser!!.uid) {
                holder.itemView.setOnLongClickListener {
                    AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Are you sure you want to delete this message")
                        .setPositiveButton("Yes") { dialog, _ ->
                            val db = FirebaseFirestore.getInstance()
                            val path = if (comment.isReel) {
                                comment.docId!! + REEL + COMMENT
                            } else {
                                comment.docId!! + POST + COMMENT
                            }
                            db.collection(path)
                                .document(comment.uid + comment.time)
                                .delete().addOnSuccessListener {
                                    if (comment.isReel) {
                                        Firebase.firestore.collection(REEL)
                                            .document(comment.docId!!)
                                            .update(
                                                "comments",
                                                FieldValue.increment(-1)
                                            ).addOnFailureListener {
                                                // its a post comment
                                            }
                                    }
                                    Toast.makeText(context, "comment deleted", Toast.LENGTH_SHORT)
                                        .show()
                                }.addOnFailureListener {
                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            notifyItemRemoved(position)
                            dialog.dismiss()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                    false
                }
            }
        }catch (e: Exception){
            Toast.makeText(context, "Error Occurred", Toast.LENGTH_SHORT).show()
        }
    }
}