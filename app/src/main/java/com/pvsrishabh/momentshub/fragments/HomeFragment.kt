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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.models.Post
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.utils.FOLLOW
import com.pvsrishabh.momentshub.utils.POST
import com.pvsrishabh.momentshub.utils.USER_NODE
import com.pvsrishabh.momentshub.adapters.FollowAdapter
import com.pvsrishabh.momentshub.adapters.PostAdapter
import com.pvsrishabh.momentshub.databinding.FragmentHomeBinding
import com.pvsrishabh.momentshub.ui.ChatActivity
import com.pvsrishabh.momentshub.ui.LikedPostsActivity

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding

    private lateinit var postAdapter: PostAdapter

    private var postList = ArrayList<Post>()

    private var followList = ArrayList<User>()

    private lateinit var followAdapter: FollowAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(context),container,false)

        postAdapter = PostAdapter(requireContext(), postList)
        binding.rvPost.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPost.adapter = postAdapter

        followAdapter = FollowAdapter(requireContext(), followList)
        binding.rvStory.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        binding.rvStory.adapter = followAdapter

        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.materialToolbar2)


        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid+FOLLOW).get().addOnSuccessListener {
            val tempList = ArrayList<User>()
            followList.clear()
            for (i in it.documents) {
                val user: User = i.toObject<User>()!!
                tempList.add(user)
            }
            followList.addAll(tempList)
            followAdapter.notifyDataSetChanged()
        }

        Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid).get().addOnSuccessListener {
            val user = it.toObject<User>()
            Glide.with(requireContext()).load(user!!.image).placeholder(R.drawable.user_icon)
                .into(binding.profileImage)
        }

        Firebase.firestore.collection(POST).get().addOnSuccessListener {
            val tempList = ArrayList<Post>()
            postList.clear()
            for (i in it.documents) {
                val post: Post = i.toObject<Post>()!!
                post.docId = i.id
                tempList.add(post)
            }
            postList.addAll(tempList)
            postAdapter.notifyDataSetChanged()
        }
        return binding.root
    }

    companion object {

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.option_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Handle other menu items if needed
            R.id.message -> {
                // Handle click on menu item
                startActivity(Intent(requireContext(),ChatActivity::class.java))
                true
            }
            R.id.like -> {
                // Handle click on menu item
                startActivity(Intent(requireContext(),LikedPostsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}