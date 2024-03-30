package com.pvsrishabh.momentshub.ui

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
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
import com.pvsrishabh.momentshub.databinding.ActivityLoginBinding
import com.pvsrishabh.momentshub.models.User
import com.pvsrishabh.momentshub.utils.USER_NODE

class LoginActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private lateinit var progressDialogForLogin: ProgressDialog

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
                            if (it.result?.additionalUserInfo?.isNewUser == true) {
                                val users = User().apply {
                                    userId = currUser?.uid
                                    name = currUser?.displayName
                                    email = currUser?.email
                                    bio = currUser?.email
                                    image = currUser?.photoUrl?.toString()
                                }
                                Firebase.firestore.collection(USER_NODE).document(currUser!!.uid)
                                    .set(users)
                                    .addOnSuccessListener {
                                        progressDialogForLogin.dismiss()
                                        startActivity(
                                            Intent(
                                                this@LoginActivity,
                                                HomeActivity::class.java
                                            )
                                        )
                                    }
                            } else {
                                progressDialogForLogin.dismiss()
                                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                finish()
                            }
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                it.exception?.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this@LoginActivity, "Failed", Toast.LENGTH_SHORT).show()
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
        googleSignInClient = GoogleSignIn.getClient(this@LoginActivity, gso)

        auth = Firebase.auth

        progressDialogForLogin = ProgressDialog(this)
        progressDialogForLogin.setTitle("Login")
        progressDialogForLogin.setMessage("Login to your account")

        binding.signInGoogleBtn.setOnClickListener {
            val signInClient = googleSignInClient.signInIntent
            signInLauncher.launch(signInClient)
        }

        binding.loginbtn.setOnClickListener {
            if (binding.email.editText?.text.toString().equals("") or
                binding.password.editText?.text.toString().equals("")
            ) {
                Toast.makeText(
                    this@LoginActivity,
                    "Please fill all the information",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                var user: User = User(
                    binding.email.editText?.text.toString(),
                    binding.password.editText?.text.toString()
                )
                progressDialogForLogin.show()
                Firebase.auth.signInWithEmailAndPassword(user.email!!, user.password!!)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            progressDialogForLogin.dismiss()
                            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                it.exception?.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        binding.signUp.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
            finish()
        }
    }
}