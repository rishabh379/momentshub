package com.pvsrishabh.momentshub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pvsrishabh.momentshub.models.Reel
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.databinding.ReelDesignBinding
import com.squareup.picasso.Picasso

class ReelAdapter(var context: Context, var reelList: ArrayList<Reel>): RecyclerView.Adapter<ReelAdapter.ViewHolder>() {
    inner class ViewHolder(var binding: ReelDesignBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ReelDesignBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return reelList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Picasso.get().load(reelList.get(position).profileLink).placeholder(R.drawable.user).into(holder.binding.profile)
        holder.binding.caption.setText(reelList.get(position).caption)
        holder.binding.videoView.setVideoPath(reelList.get(position).videoUrl)
        holder.binding.videoView.setOnPreparedListener {
            holder.binding.progressBar.visibility = View.GONE
            holder.binding.videoView.start()
        }
    }
}