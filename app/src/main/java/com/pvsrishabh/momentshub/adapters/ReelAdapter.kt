package com.pvsrishabh.momentshub.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.databinding.ReelDesignBinding
import com.pvsrishabh.momentshub.models.Reel
import com.pvsrishabh.momentshub.ui.OthersProfileActivity
import com.pvsrishabh.momentshub.utils.LIKE
import com.pvsrishabh.momentshub.utils.POST
import com.pvsrishabh.momentshub.utils.REEL
import com.pvsrishabh.momentshub.utils.REELLIKE
import com.pvsrishabh.momentshub.utils.extractAndTrimFirstString
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
        holder.binding.likesCount.text = reelList[position].likes.toString()

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
            }


        holder.binding.like.setOnClickListener {
            if (like == 0) {
                holder.binding.like.setImageResource(R.drawable.heart)
                val nlikesCount: Long = holder.binding.likesCount.text.toString().toLong() + 1
                holder.binding.likesCount.text = "${nlikesCount}"
                db.collection(currUserId + REELLIKE)
                    .document(reelList[position].docId!!).set(reelList[position])
                    .addOnSuccessListener {
                        like = 1
                        // increase likes count
                        val reelDocRef =
                            Firebase.firestore.collection(REEL).document(reelList[position].docId!!)

                        Firebase.firestore.runTransaction { transaction ->
                            val snapshot = transaction.get(reelDocRef)
                            val likes = snapshot.getLong("likes") ?: 0
                            transaction.update(reelDocRef, "likes", likes + 1)
                        }.addOnSuccessListener {
                            // Update successful
                        }.addOnFailureListener { e ->
                            // Handle any errors
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
            } else {
                holder.binding.like.setImageResource(R.drawable.like)
                var likesCount: Long = holder.binding.likesCount.text.toString().toLong()
                if(likesCount.toInt() <= 0){
                    likesCount = 1
                }
                val nlikesCount = likesCount - 1
                holder.binding.likesCount.text = "${nlikesCount}"
                db.collection(currUserId + LIKE)
                    .document(reelList[position].docId!!).delete().addOnSuccessListener {
                        like = 0
                        // decrease likes count
                        val reelDocRef =
                            Firebase.firestore.collection(REEL).document(reelList[position].docId!!)

                        Firebase.firestore.runTransaction { transaction ->
                            val snapshot = transaction.get(reelDocRef)
                            val likes = snapshot.getLong("likes") ?: 1
                            transaction.update(reelDocRef, "likes", likes - 1)
                        }.addOnSuccessListener {
                            // Update successful
                        }.addOnFailureListener { e ->
                            // Handle any errors
                        }
                    }
            }
        }

    }
}