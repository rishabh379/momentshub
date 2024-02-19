package com.pvsrishabh.momentshub.ui

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.pvsrishabh.momentshub.Models.User
import com.pvsrishabh.momentshub.Utils.USER_NODE
import com.pvsrishabh.momentshub.Utils.USER_PROFILE_FOLDER
import com.pvsrishabh.momentshub.Utils.uploadImage
import com.pvsrishabh.momentshub.databinding.ActivitySignUpBinding
import com.squareup.picasso.Picasso

class SignUpActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }
    private lateinit var user: User
    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploadImage(uri, USER_PROFILE_FOLDER) {
                if (it != null) {
                    user.image = it
                    binding.profileImage.setImageURI(uri)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val text =
            "<font color=#FF000000>Already have an account?</font><font color=#1E88E5> Login</font>"
        binding.login.text = Html.fromHtml(text)
        user = User()

        if (intent.hasExtra("MODE")) {
            if (intent.getIntExtra("MODE", -1) == 1) {

//              my doings
                if(true){
                    binding.email.visibility = View.GONE
                    binding.password.visibility = View.GONE
                    binding.login.visibility = View.GONE

                    val constraintLayout: ConstraintLayout = binding.myConstraintLayout
                    val myButton = binding.signUpBtn
                    val myEditText = binding.name

                    val constraintSet = ConstraintSet()
                    constraintSet.clone(constraintLayout)
                    constraintSet.connect(
                        myButton.id,
                        ConstraintSet.TOP,
                        myEditText.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSet.connect(
                        myButton.id,
                        ConstraintSet.START,
                        myEditText.id,
                        ConstraintSet.START,
                        0
                    )
                    constraintSet.connect(
                        myButton.id,
                        ConstraintSet.END,
                        myEditText.id,
                        ConstraintSet.END,
                        0
                    )
                    constraintSet.connect(
                        myButton.id,
                        ConstraintSet.BOTTOM,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSet.setVerticalBias(myButton.id, 0.3f)
                    constraintSet.applyTo(constraintLayout)
                }

                binding.signUpBtn.text = "Update Profile"
                Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid)
                    .get().addOnSuccessListener {
                        user = it.toObject<User>()!!
                        if (!user.image.isNullOrEmpty()) {
                            Picasso.get().load(user.image).into(binding.profileImage)
                        }
                        binding.name.editText?.setText(user.name)
                    }
            }
        }
        binding.signUpBtn.setOnClickListener {
            if (intent.hasExtra("MODE")) {
                if (intent.getIntExtra("MODE", -1) == 1) {
                    val db = Firebase.firestore
                    user.name = binding.name.editText?.text.toString()
                    db.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid)
                        .set(user).addOnSuccessListener {
                            startActivity(
                                Intent(
                                    this@SignUpActivity,
                                    HomeActivity::class.java
                                )
                            )
                            finish()
                        }
                }
            } else {
                if (binding.name.editText?.text.toString().equals("") or
                    binding.email.editText?.text.toString().equals("") or
                    binding.password.editText?.text.toString().equals("")
                ) {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Please fill all the information",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                        binding.email.editText?.text.toString(),
                        binding.password.editText?.text.toString()
                    ).addOnCompleteListener { result ->
                        if (result.isSuccessful) {
                            user.name = binding.name.editText?.text.toString()
                            user.password = binding.password.editText?.text.toString()
                            user.email = binding.email.editText?.text.toString()

                            val db = Firebase.firestore
                            db.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid)
                                .set(user)
                                .addOnSuccessListener {
                                    startActivity(
                                        Intent(
                                            this@SignUpActivity,
                                            HomeActivity::class.java
                                        )
                                    )
                                    finish()
                                }

                        } else {
                            Toast.makeText(
                                this@SignUpActivity,
                                result.exception?.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
        binding.addImage.setOnClickListener {
            launcher.launch("image/*")
        }
        binding.login.setOnClickListener {
            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
            finish()
        }
    }
}