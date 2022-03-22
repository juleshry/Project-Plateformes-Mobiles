package com.example.projectplateformesmobiles.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.projectplateformesmobiles.R

class Account : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        val backButton: Button = findViewById(R.id.account_back_button)
        backButton.setOnClickListener{
            this.finish()
        }
    }
}