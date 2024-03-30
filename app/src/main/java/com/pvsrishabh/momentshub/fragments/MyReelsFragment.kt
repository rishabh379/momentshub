package com.pvsrishabh.momentshub.fragments

import android.app.AlertDialog
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
import com.pvsrishabh.momentshub.adapters.MyReelRvAdapter
import com.pvsrishabh.momentshub.databinding.FragmentMyReelsBinding
import com.pvsrishabh.momentshub.models.Reel
import com.pvsrishabh.momentshub.utils.REEL

class MyReelsFragment : Fragment(), MyReelRvAdapter.AdapterCallback {


    companion object {
        private const val ARG_UID = "uid"

        // Factory method to create a new instance of MyPostFragment with a uid parameter
        fun newInstance(uid: String): MyReelsFragment {
            val fragment = MyReelsFragment()
            val args = Bundle()
            args.putString(ARG_UID, uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var uid: String? = null


    private lateinit var binding: FragmentMyReelsBinding
    private var reelList = ArrayList<Reel>()
    private lateinit var adapter: MyReelRvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uid = it.getString(MyReelsFragment.ARG_UID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyReelsBinding.inflate(inflater, container, false)

        adapter = MyReelRvAdapter(requireContext(), reelList, this)
        binding.rv.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.rv.adapter = adapter

        if(uid.isNullOrEmpty()){
            uid = Firebase.auth.currentUser!!.uid
        }

        Firebase.firestore.collection(uid + REEL).get()
            .addOnSuccessListener {
                reelList.clear()
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

    override fun onItemLongClicked(position: Int, reel: Reel) {
        val currUid = FirebaseAuth.getInstance().uid
        if (uid == currUid) {
            AlertDialog.Builder(context)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this post")
                .setPositiveButton("Yes") { dialog, _ ->
                    Firebase.firestore.collection(currUid + REEL)
                        .whereEqualTo("time", reel.time).get().addOnSuccessListener {
                            for (document in it.documents) {
                                document.reference.delete()
                                    .addOnSuccessListener {
                                        Firebase.firestore.collection(REEL)
                                            .whereEqualTo(
                                                "uid",
                                                currUid
                                            ) // Filter by userId
                                            .whereEqualTo("time", reel.time) // Filter by time
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
                    reelList.removeAt(position)
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