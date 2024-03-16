package com.pvsrishabh.momentshub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.models.Reel
import com.pvsrishabh.momentshub.utils.REEL
import com.pvsrishabh.momentshub.adapters.MyReelRvAdapter
import com.pvsrishabh.momentshub.databinding.FragmentMyReelsBinding

class MyReelsFragment : Fragment() {

    private lateinit var binding: FragmentMyReelsBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyReelsBinding.inflate(inflater, container, false)

        val reelList = ArrayList<Reel>()
        val adapter = MyReelRvAdapter(requireContext(), reelList)
        binding.rv.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.rv.adapter = adapter

        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + REEL).get()
            .addOnSuccessListener {
                val tempList = arrayListOf<Reel>()
                for (i in it.documents) {
                    val reel: Reel = i.toObject<Reel>()!!
                    tempList.add(reel)
                }
                reelList.addAll(tempList)
                adapter.notifyDataSetChanged()
            }

        return binding.root
    }

    companion object {

    }
}