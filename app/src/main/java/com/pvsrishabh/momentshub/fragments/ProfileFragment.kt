package com.pvsrishabh.momentshub.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.ui.SignUpActivity
import com.pvsrishabh.momentshub.utils.USER_NODE
import com.pvsrishabh.momentshub.adapters.ViewPagerAdapter
import com.pvsrishabh.momentshub.databinding.FragmentProfileBinding
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater,container, false)

        binding.editProfile.setOnClickListener {
            val intent = Intent(activity, SignUpActivity::class.java)
            intent.putExtra("MODE",1)
            activity?.startActivity(intent)
            activity?.finish()
        }

        viewPagerAdapter = ViewPagerAdapter(requireActivity().supportFragmentManager)
        viewPagerAdapter.addFragment(MyPostFragment(),"My Post")
        viewPagerAdapter.addFragment(MyReelsFragment(),"My Reels")
        binding.viewPager.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        return binding.root
    }

    companion object {

    }

    override fun onStart() {
        super.onStart()

        Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid).get().addOnSuccessListener {
            val user: User = it.toObject<User>()!!
            binding.tvName.text = user.name
            binding.tvBio.text = user.email

            if(!user.image.isNullOrEmpty()){
                Picasso.get().load(user.image).into(binding.profileImage)
            }

        }
    }
}