package com.pvsrishabh.momentshub.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pvsrishabh.momentshub.R
import com.pvsrishabh.momentshub.databinding.ActivityShowListBinding

class ShowListActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityShowListBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        setSupportActionBar(binding.materialToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.materialToolbar.setNavigationOnClickListener {
            startActivity(
                Intent(this@ShowListActivity, HomeActivity::class.java)
            )
            finish()
        }
    }
}