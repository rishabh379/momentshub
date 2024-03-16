package com.pvsrishabh.momentshub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.utils.USER_NODE
import com.pvsrishabh.momentshub.adapters.SearchAdapter
import com.pvsrishabh.momentshub.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    lateinit var binding: FragmentSearchBinding
    lateinit var adapter: SearchAdapter
    private var userList = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        binding.rv.layoutManager = LinearLayoutManager(requireContext())
        adapter= SearchAdapter(requireContext(),userList)
        binding.rv.adapter = adapter

        val currentUser: String = Firebase.auth.currentUser!!.uid
        Firebase.firestore.collection(USER_NODE).get().addOnSuccessListener {
            val tempList = ArrayList<User>()
            userList.clear()
            for(i in it.documents){
                if(i.id != currentUser) {
                    val user = i.toObject<User>()!!
                    tempList.add(user)
                }
            }
            userList.addAll(tempList)
            adapter.notifyDataSetChanged()
        }

        binding.searchButton.setOnClickListener {
            val text = binding.userName.text.toString()
            Firebase.firestore.collection(USER_NODE).whereEqualTo("name",text).get().addOnSuccessListener {
                val tempList = ArrayList<User>()
                userList.clear()
                if(!it.isEmpty){
                    for(i in it.documents){
                        if(i.id != currentUser) {
                            val user = i.toObject<User>()!!
                            tempList.add(user)
                        }
                    }
                    userList.addAll(tempList)
                    adapter.notifyDataSetChanged()
                }
            }
        }
        return binding.root
    }
}