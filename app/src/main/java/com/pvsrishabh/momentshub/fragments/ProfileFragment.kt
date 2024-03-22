package com.pvsrishabh.momentshub.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.adapters.ViewPagerAdapter
import com.pvsrishabh.momentshub.databinding.FragmentProfileBinding
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.ui.EditProfileActivity
import com.pvsrishabh.momentshub.ui.FollowActivity
import com.pvsrishabh.momentshub.ui.LoginActivity
import com.pvsrishabh.momentshub.ui.SavedPostsActivity
import com.pvsrishabh.momentshub.utils.FOLLOW
import com.pvsrishabh.momentshub.utils.FOLLOWERS
import com.pvsrishabh.momentshub.utils.POST
import com.pvsrishabh.momentshub.utils.USER_NODE
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var tUser: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater,container, false)

        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.materialToolbar2)

        binding.editProfile.setOnClickListener {
            activity?.startActivity(Intent(activity, EditProfileActivity::class.java))
            activity?.finish()
        }

        binding.followersLl.setOnClickListener {
            val intent = Intent(activity, FollowActivity::class.java)
            intent.putExtra("uid",Firebase.auth.currentUser!!.uid)
            intent.putExtra("type", FOLLOWERS)
            activity?.startActivity(intent)
            activity?.finish()
        }

        binding.followingLl.setOnClickListener {
            val intent = Intent(activity, FollowActivity::class.java)
            intent.putExtra("uid",Firebase.auth.currentUser!!.uid)
            intent.putExtra("type", FOLLOW)
            activity?.startActivity(intent)
            activity?.finish()
        }

        auth = FirebaseAuth.getInstance()

        viewPagerAdapter = ViewPagerAdapter(requireActivity().supportFragmentManager,  auth.uid!!)
        viewPagerAdapter.addFragment(MyPostFragment(),"My Post")
        viewPagerAdapter.addFragment(MyReelsFragment(),"My Reels")
        binding.viewPager.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        viewPagerAdapter = ViewPagerAdapter(requireActivity().supportFragmentManager,  auth.uid!!)
        viewPagerAdapter.addFragment(MyPostFragment(),"My Post")
        viewPagerAdapter.addFragment(MyReelsFragment(),"My Reels")
        binding.viewPager.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    companion object {
    }

    override fun onStart() {
        super.onStart()

        val db = Firebase.firestore

        db.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid).get().addOnSuccessListener {
            val user: User = it.toObject<User>()!!
            binding.materialToolbar2.title = user.name
            tUser = user
            binding.tvName.text = user.name
            if(user.bio == null){
                binding.tvBio.text = user.email
            }else{
                binding.tvBio.text = user.bio
            }
            binding.followingCount.text = (user.followingCount?: 0).toString()
            binding.postCount.text = (user.postCount?: 0).toString()
            binding.followersCount.text = (user.followersCount?: 0).toString()
            if(!user.image.isNullOrEmpty()){
                Picasso.get().load(user.image).into(binding.profileImage)
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private val gso = GoogleSignInOptions
        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("998162591486-cl86vjo5403hiucgujbh2omes1hb0ecp.apps.googleusercontent.com")
        .requestEmail()
        .build()

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Handle other menu items if needed
            R.id.saved -> {
                // Handle click on menu item
                activity?.startActivity(Intent(requireContext(), SavedPostsActivity::class.java))
                true
            }
            R.id.logOut -> {
                // Handle click on menu item
                if(tUser.password == null){
                    GoogleSignIn.getClient(requireActivity(),gso).signOut()
                }else{
                    auth.signOut()
                }
                activity?.startActivity(Intent(requireContext(), LoginActivity::class.java))
                activity?.finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}