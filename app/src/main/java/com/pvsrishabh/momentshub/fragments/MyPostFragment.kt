package com.pvsrishabh.momentshub.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.adapters.MyPostRvAdapter
import com.pvsrishabh.momentshub.databinding.FragmentMyPostBinding
import com.pvsrishabh.momentshub.models.Post
import com.pvsrishabh.momentshub.ui.LikedPostsActivity
import com.pvsrishabh.momentshub.ui.ShowListActivity
import com.pvsrishabh.momentshub.utils.POST
import com.pvsrishabh.momentshub.utils.changePostCount

class MyPostFragment : Fragment(), MyPostRvAdapter.AdapterCallback {

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
    private lateinit var postList: ArrayList<Post>
    private lateinit var adapter: MyPostRvAdapter

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

        postList = ArrayList<Post>()
        adapter = MyPostRvAdapter(requireContext(), postList, this)
        binding.rv.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.rv.adapter = adapter

        if (uid.isNullOrEmpty()) {
            uid = Firebase.auth.currentUser!!.uid
        }

        Firebase.firestore.collection(uid + POST).get().addOnSuccessListener {
            val tempList = arrayListOf<Post>()
            postList.clear()
            for (i in it.documents) {
                val post: Post = i.toObject<Post>()!!
                tempList.add(post)
            }
            postList.addAll(tempList)
            adapter.notifyDataSetChanged()
        }

        return binding.root
    }

    override fun onItemLongClicked(position: Int, post: Post) {
        val currUid = FirebaseAuth.getInstance().uid
        if (uid == currUid) {
            AlertDialog.Builder(context)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this post")
                .setPositiveButton("Yes") { dialog, _ ->
                    Firebase.firestore.collection(currUid + POST)
                        .whereEqualTo("time", post.time).get().addOnSuccessListener {
                            for (document in it.documents) {
                                document.reference.delete()
                                    .addOnSuccessListener {
                                        changePostCount(-1)
                                        Firebase.firestore.collection(POST)
                                            .whereEqualTo(
                                                "uid",
                                                currUid
                                            ) // Filter by userId
                                            .whereEqualTo("time", post.time) // Filter by time
                                            .get()
                                            .addOnSuccessListener { querySnapshot ->
                                                for (doc in querySnapshot.documents) {
                                                    doc.reference.delete()
                                                }
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    context,
                                                    "Can't Delete from Posts Section",
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()
                                            }
                                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Can't delete",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                            }
                        }
                    postList.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}