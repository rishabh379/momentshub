package com.pvsrishabh.momentshub.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pvsrishabh.momentshub.Models.Message
import com.pvsrishabh.momentshub.Utils.CHAT
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
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            false
        }

        if (holder is SenderViewHolder) {
            holder.bind(messageModel)
        } else if (holder is ReceiverViewHolder) {
            holder.bind(messageModel)
        }
    }

    override fun getItemCount(): Int {
        return messageModels.size
    }

    inner class ReceiverViewHolder(private val binding: SampleReceiverBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(messageModel: Message) {
            binding.receiverText.text = messageModel.text
            binding.receiverTime.text = messageModel.time

        }
    }

    inner class SenderViewHolder(private val binding: SampleSenderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(messageModel: Message) {
            binding.senderMessage.text = messageModel.text
            binding.senderTime.text = messageModel.time
        }
    }
}
