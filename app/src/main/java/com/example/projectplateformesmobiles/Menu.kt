package com.example.projectplateformesmobiles

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle


class Menu : AppCompatActivity() {
    private companion object {
        private const val TAG = "Menu"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

    }
}