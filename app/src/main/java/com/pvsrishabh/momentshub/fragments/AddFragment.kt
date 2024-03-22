package com.pvsrishabh.momentshub.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pvsrishabh.momentshub.databinding.FragmentAddBinding
import com.pvsrishabh.momentshub.ui.PostActivity
import com.pvsrishabh.momentshub.ui.ReelsActivity

class AddFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddBinding.inflate(inflater,container, false)

        binding.post.setOnClickListener {
            activity?.startActivity(Intent(requireContext(),PostActivity::class.java))
        }

        binding.reel.setOnClickListener {
            activity?.startActivity(Intent(requireContext(),ReelsActivity::class.java))
        }

        return binding.root
    }

    companion object {

    }
}