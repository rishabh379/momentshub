package com.pvsrishabh.momentshub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.models.Reel
import com.pvsrishabh.momentshub.utils.REEL
import com.pvsrishabh.momentshub.adapters.ReelAdapter
import com.pvsrishabh.momentshub.databinding.FragmentReelBinding

class ReelFragment : Fragment() {

    private lateinit var binding: FragmentReelBinding

    private lateinit var adapter: ReelAdapter

    var reelList = ArrayList<Reel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReelBinding.inflate(inflater, container, false)

        adapter = ReelAdapter(requireContext(), reelList)

        binding.viewPager.adapter = adapter

        Firebase.firestore.collection(REEL).get().addOnSuccessListener {
            val tempList = ArrayList<Reel>()
            reelList.clear()
            for (i in it.documents) {
                val reel: Reel = i.toObject<Reel>()!!
                reel.docId = i.id
                tempList.add(reel)
            }
            reelList.addAll(tempList)
            reelList.reverse()
            adapter.notifyDataSetChanged()
        }
        return binding.root
    }

    companion object {

    }
}