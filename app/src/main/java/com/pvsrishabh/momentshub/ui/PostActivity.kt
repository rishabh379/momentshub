package com.pvsrishabh.momentshub.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.models.Post
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.utils.POST
import com.pvsrishabh.momentshub.utils.POST_FOLDER
import com.pvsrishabh.momentshub.utils.USER_NODE
import com.pvsrishabh.momentshub.utils.uploadImage
import com.pvsrishabh.momentshub.databinding.ActivityPostBinding
import com.pvsrishabh.momentshub.utils.REEL_FOLDER
import com.pvsrishabh.momentshub.utils.uploadVideo

class PostActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityPostBinding.inflate(layoutInflater)
    }
    var imageUrl: String? = null
    private lateinit var progressDialog: ProgressDialog
    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploadImage(uri, POST_FOLDER, progressDialog) { url ->
                if (url != null) {
                    binding.selectImage.setImageURI(uri)
                    binding.postBtn.isEnabled = true
                    imageUrl = url
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)

        setSupportActionBar(binding.materialToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.materialToolbar.setNavigationOnClickListener {
            startActivity(
                Intent(this@PostActivity, HomeActivity::class.java)
            )
            finish()
        }

        binding.selectImage.setOnClickListener {
            launcher.launch("image/*")
        }

        binding.postBtn.isEnabled = false

        binding.postBtn.setOnClickListener {
            Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid).get()
                .addOnSuccessListener {
                    val user = it.toObject<User>()
                    val post: Post = Post(
                        imageUrl!!,
                        binding.caption.editText?.text.toString(),
                        Firebase.auth.currentUser!!.uid,
                        System.currentTimeMillis().toString()
                    )

                    Firebase.firestore.collection(POST).document().set(post).addOnSuccessListener {
                        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid+POST).document()
                            .set(post)
                            .addOnSuccessListener {
                                startActivity(
                                    Intent(this@PostActivity, HomeActivity::class.java)
                                )
                                finish()
                            }
                    }
                }
        }

        binding.cancelBtn.setOnClickListener {
            startActivity(
                Intent(this@PostActivity, HomeActivity::class.java)
            )
            finish()
        }
    }
}