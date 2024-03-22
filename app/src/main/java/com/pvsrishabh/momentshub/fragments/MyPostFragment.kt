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
import com.pvsrishabh.momentshub.models.Post
import com.pvsrishabh.momentshub.adapters.MyPostRvAdapter
import com.pvsrishabh.momentshub.databinding.FragmentMyPostBinding
import com.pvsrishabh.momentshub.utils.POST

class MyPostFragment : Fragment() {

    companion object {
        private const val ARG_UID = "uid"

        // Factory method to create a new instance of MyPostFragment with a uid parameter
        fun newInstance(uid: String): MyPostFragment {
            val fragment = MyPostFragment()
            val args = Bundle()
            args.putString(ARG_UID, uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var uid: String? = null

    private lateinit var binding: FragmentMyPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uid = it.getString(ARG_UID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyPostBinding.inflate(inflater, container, false)

        val postList = ArrayList<Post>()
        val adapter = MyPostRvAdapter(requireContext(), postList)
        binding.rv.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.rv.adapter = adapter

        if(uid.isNullOrEmpty()){
            uid = Firebase.auth.currentUser!!.uid
        }

        Firebase.firestore.collection(uid+POST).get().addOnSuccessListener {
            val tempList = arrayListOf<Post>()
            for(i in it.documents){
                val post: Post = i.toObject<Post>()!!
                tempList.add(post)
            }
            postList.addAll(tempList)
            adapter.notifyDataSetChanged()
        }

        return binding.root
    }
}