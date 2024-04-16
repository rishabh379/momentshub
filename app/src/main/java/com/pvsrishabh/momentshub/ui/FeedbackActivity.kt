package com.pvsrishabh.momentshub.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pvsrishabh.momentshub.databinding.ActivityFeedbackBinding

class FeedbackActivity : AppCompatActivity() {
    val binding by lazy {
        ActivityFeedbackBinding.inflate(layoutInflater)
    }

    override fun onRestart() {
        super.onRestart()
        startActivity(Intent(this@FeedbackActivity, HomeActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@FeedbackActivity, HomeActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.materialToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.materialToolbar.setNavigationOnClickListener {
            startActivity(
                Intent(this@FeedbackActivity, HomeActivity::class.java)
            )
            finish()
        }

        binding.postBtn.setOnClickListener {
            if (binding.etFeedback.editText?.text.toString().equals("")) {
                Toast.makeText(this@FeedbackActivity, "Enter your Feedback", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val text = binding.etFeedback.editText?.text.toString()
                sendEmail(text)
                binding.etFeedback.editText?.setText("")
            }
        }

        binding.cancelBtn.setOnClickListener {
            startActivity(
                Intent(this@FeedbackActivity, HomeActivity::class.java)
            )
            finish()
        }
    }

    private fun sendEmail(text: String) {
        val recipientEmail = "prishabhvishwas@gmail.com" // Developer's email address
        val subject = "Feedback/Issue Report" // Subject of the email
        val message = "Dear Developer, $text" // Body of the email

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$recipientEmail")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
        }

        // Specify the package name of the Gmail app
        intent.setPackage("com.google.android.gm")

        // Check if there are any activities available to handle the intent
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // If no activity is available, display a message to the user
            Toast.makeText(this@FeedbackActivity, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }
}