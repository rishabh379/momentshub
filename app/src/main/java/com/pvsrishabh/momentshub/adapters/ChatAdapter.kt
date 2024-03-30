package com.pvsrishabh.momentshub.adapters

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.models.Message
import com.pvsrishabh.momentshub.utils.CHAT
import com.pvsrishabh.momentshub.databinding.SampleReceiverBinding
import com.pvsrishabh.momentshub.databinding.SampleSenderBinding

class ChatAdapter(
    private val messageModels: ArrayList<Message>,
    private val context: Context,
    private val recId: String? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val SENDER_VIEW_TYPE = 1
    private val RECEIVER_VIEW_TYPE = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == SENDER_VIEW_TYPE) {
            val binding =
                SampleSenderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SenderViewHolder(binding)
        } else {
            val binding =
                SampleReceiverBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceiverViewHolder(binding)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageModels[position].msgId == FirebaseAuth.getInstance().uid) {
            SENDER_VIEW_TYPE
        } else {
            RECEIVER_VIEW_TYPE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val messageModel = messageModels[position]

        try {
            messageModel.time = TimeAgo.using(messageModels[position].time.toLong())
        } catch (e: Exception) {
            messageModel.time = ""
        }

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this message")
                .setPositiveButton("Yes") { dialog, _ ->
                    val db = FirebaseFirestore.getInstance()
                    val senderRoom = "${FirebaseAuth.getInstance().uid}$recId"
                    db.collection(CHAT)
                        .document(senderRoom)
                        .collection("messages")
                        .document(messageModel.uId)
                        .delete()
                    notifyItemRemoved(position)
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            false
        }

        if (holder is SenderViewHolder) {
            if (messageModel.isImage) {
                holder.binding.senderMessage.visibility = View.INVISIBLE
                holder.setImage(messageModel.text)
            }else{
                holder.binding.ivMessage.visibility = View.GONE
            }
            holder.bind(messageModel)
        } else if (holder is ReceiverViewHolder) {
            if (messageModel.isImage) {
                holder.binding.receiverText.visibility = View.INVISIBLE
                holder.setImage(messageModel.text)
            }else{
                holder.binding.ivMessage.visibility = View.GONE
            }
            holder.bind(messageModel)
        }
    }

    override fun getItemCount(): Int {
        return messageModels.size
    }

    inner class ReceiverViewHolder(val binding: SampleReceiverBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(messageModel: Message) {
            binding.receiverText.text = messageModel.text
            binding.receiverTime.text = messageModel.time
        }
        fun setImage(imageUrl: String) {
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.loading)
                .into(binding.ivMessage)
        }
    }

    inner class SenderViewHolder(val binding: SampleSenderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(messageModel: Message) {
            binding.senderMessage.text = messageModel.text
            binding.senderTime.text = messageModel.time
        }
        fun setImage(imageUrl: String) {
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.loading)
                .into(binding.ivMessage)
        }
    }
}