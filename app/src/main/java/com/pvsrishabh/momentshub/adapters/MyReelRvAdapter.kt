package com.pvsrishabh.momentshub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.pvsrishabh.momentshub.models.Reel
import com.pvsrishabh.momentshub.databinding.MyReelRvDesignBinding

class MyReelRvAdapter(var context: Context, var reelList: ArrayList<Reel>) : RecyclerView.Adapter<MyReelRvAdapter.ViewHolder>(){

    inner class ViewHolder(var binding: MyReelRvDesignBinding): RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MyReelRvDesignBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return reelList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context)
            .load(reelList.get(position).videoUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.binding.reelImage)

    }
}