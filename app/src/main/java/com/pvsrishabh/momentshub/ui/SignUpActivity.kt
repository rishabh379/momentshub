package com.pvsrishabh.momentshub.ui

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.pvsrishabh.momentshub.databinding.ActivitySignUpBinding
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.utils.USER_NODE
import com.pvsrishabh.momentshub.utils.USER_PROFILE_FOLDER
import com.pvsrishabh.momentshub.utils.uploadImage

class SignUpActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }
    private lateinit var user: User
    var imageUrl: String? = null
    private lateinit var progressDialog: ProgressDialog
    private lateinit var progressDialogForLogin: ProgressDialog
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

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    val account: GoogleSignInAccount? = task.result
                    val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                    progressDialogForLogin.show()
                    auth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val currUser = auth.currentUser
                            if(it.result?.additionalUserInfo?.isNewUser == true){
                                val users = User().apply {
                                    userId = currUser?.uid
                                    name = currUser?.displayName
                                    email = currUser?.email
                                    bio = currUser?.email
                                    image = if (imageUrl == null) {
                                        currUser?.photoUrl?.toString()
                                    } else {
                                        imageUrl
                                    }
                                }
                                Firebase.firestore.collection(USER_NODE).document(currUser!!.uid)
                                    .set(users)
                                    .addOnSuccessListener {
                                        progressDialogForLogin.dismiss()
                                        startActivity(
                                            Intent(
                                                this@SignUpActivity,
                                                HomeActivity::class.java
                                            )
                                        )
                                    }
                            }else{
                                progressDialogForLogin.dismiss()
                                startActivity(Intent(this@SignUpActivity, HomeActivity::class.java))
                                finish()
                            }
                        } else {
                            Toast.makeText(this@SignUpActivity, "Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this@SignUpActivity, "Failed", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("998162591486-cl86vjo5403hiucgujbh2omes1hb0ecp.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this@SignUpActivity, gso)

        auth = Firebase.auth

        progressDialog = ProgressDialog(this)

        progressDialogForLogin = ProgressDialog(this)
        progressDialogForLogin.setTitle("Login")
        progressDialogForLogin.setMessage("Login to your account")

        val text =
            "<font color=#FF000000>Already have an account?</font><font color=#1E88E5> Login</font>"
        binding.login.text = Html.fromHtml(text)
        user = User()

        binding.signUpGoogleBtn.setOnClickListener {
            val signInClient = googleSignInClient.signInIntent
            signInLauncher.launch(signInClient)
        }

        binding.signUpBtn.setOnClickListener {
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
                progressDialogForLogin.show()
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    binding.email.editText?.text.toString(),
                    binding.password.editText?.text.toString()
                ).addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        user.name = binding.name.editText?.text.toString()
                        user.password = binding.password.editText?.text.toString()
                        user.email = binding.email.editText?.text.toString()
                        user.userId = Firebase.auth.currentUser!!.uid
                        user.image = imageUrl

                        val db = Firebase.firestore
                        db.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid)
                            .set(user)
                            .addOnSuccessListener {
                                progressDialogForLogin.dismiss()
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
        binding.addImage.setOnClickListener {
            launcher.launch("image/*")
        }
        binding.login.setOnClickListener {
            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
            finish()
        }
    }
}