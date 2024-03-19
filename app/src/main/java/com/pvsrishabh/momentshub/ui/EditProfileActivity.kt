package com.pvsrishabh.momentshub.ui

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.databinding.ActivityEditProfileBinding
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.utils.USER_NODE
import com.pvsrishabh.momentshub.utils.USER_PROFILE_FOLDER
import com.pvsrishabh.momentshub.utils.uploadImage
import com.squareup.picasso.Picasso

class EditProfileActivity : AppCompatActivity() {
    private val binding by lazy{
        ActivityEditProfileBinding.inflate(layoutInflater)
    }
    private lateinit var user: User
    var imageUrl: String? = null
    private lateinit var progressDialog: ProgressDialog
    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploadImage(uri, USER_PROFILE_FOLDER, progressDialog) { url ->
                if (url != null) {
                    binding.profileImage.setImageURI(uri)
                    imageUrl = url
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)

        user = User()

        val db = Firebase.firestore

        db.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid)
            .get().addOnSuccessListener {
                user = it.toObject<User>()!!
                if (!user.image.isNullOrEmpty()) {
                    Picasso.get().load(user.image).into(binding.profileImage)
                }
                binding.name.editText?.setText(user.name)
                if(user.bio == null){
                    user.bio = user.email
                }
                binding.bio.editText?.setText(user.bio)
                imageUrl = user.image
            }

        binding.updateBtn.setOnClickListener {
            user.name = binding.name.editText?.text.toString()
            user.image = imageUrl
            user.bio = binding.bio.editText?.text.toString()
            db.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid)
                .set(user).addOnSuccessListener {
                    startActivity(
                        Intent(
                            this@EditProfileActivity,
                            HomeActivity::class.java
                        )
                    )
                    finish()
                }
        }

        binding.addImage.setOnClickListener {
            launcher.launch("image/*")
        }
    }
}