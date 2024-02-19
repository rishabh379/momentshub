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
import com.pvsrishabh.momentshub.Models.Reel
import com.pvsrishabh.momentshub.Models.User
import com.pvsrishabh.momentshub.Utils.REEL
import com.pvsrishabh.momentshub.Utils.REEL_FOLDER
import com.pvsrishabh.momentshub.Utils.USER_NODE
import com.pvsrishabh.momentshub.Utils.uploadVideo
import com.pvsrishabh.momentshub.databinding.ActivityReelsBinding

class ReelsActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityReelsBinding.inflate(layoutInflater)
    }
    private lateinit var videoUrl: String
    private lateinit var progressDialog: ProgressDialog
    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploadVideo(uri, REEL_FOLDER, progressDialog) { url ->
                if (url != null) {
                    videoUrl = url
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
                Intent(this@ReelsActivity, HomeActivity::class.java)
            )
            finish()
        }


        binding.selectReel.setOnClickListener {
            launcher.launch("video/*")
        }

        binding.postBtn.setOnClickListener {

            Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid)
                .get().addOnSuccessListener {
                    val user = it.toObject<User>()!!
                    val reel: Reel = Reel(videoUrl!!, binding.caption.editText?.text.toString(),user.image!!)
                    Firebase.firestore.collection(REEL).document().set(reel).addOnSuccessListener {
                        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid+REEL).document().set(reel)
                            .addOnSuccessListener {
                                startActivity(
                                    Intent(this@ReelsActivity, HomeActivity::class.java)
                                )
                                finish()
                            }
                    }
                }
        }

        binding.cancelBtn.setOnClickListener {
            startActivity(
                Intent(this@ReelsActivity, HomeActivity::class.java)
            )
            finish()
        }

    }
}