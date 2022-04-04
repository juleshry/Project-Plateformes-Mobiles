package com.example.projectplateformesmobiles.ui.accountCreation

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projectplateformesmobiles.Menu
import com.example.projectplateformesmobiles.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase


class AccountCreation : AppCompatActivity() {

    private companion object {
        private const val CreateTAG = "CreateAccountActivity"
        private const val LoginTAG = "CreateAccountActivity"
        private const val UpdateTAG = "UpdateUserActivity"
    }

    private lateinit var auth: FirebaseAuth

    private val validPassword: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_account_creation)

        auth = Firebase.auth

        val nameEditText: EditText = findViewById(R.id.name)
        val surnameEditText: EditText = findViewById(R.id.surname)
        val emailEditText: EditText = findViewById(R.id.email)
        val passwordEditText: EditText = findViewById(R.id.password)
        val confirmPasswordEditText: EditText = findViewById(R.id.confirmPassword)

        passwordEditText.addTextChangedListener( object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //TODO Check Password format
            }
        })

        confirmPasswordEditText.addTextChangedListener( object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(passwordEditText.text.toString() != s.toString()){
                    confirmPasswordEditText.setTextColor(Color.RED)
                } else {
                    confirmPasswordEditText.setTextColor(Color.WHITE)
                }
            }
        })

        val cancel: Button = findViewById(R.id.CancelNewAccount)
        cancel.setOnClickListener{
            finish()
        }

        val confirmNewAccount: Button = findViewById(R.id.ConfirmNewAccount)
        confirmNewAccount.setOnClickListener{
            if(passwordEditText.text.toString() != "" && nameEditText.text.toString() != ""
                && surnameEditText.text.toString() != "" && emailEditText.text.toString() != ""
                && passwordEditText.text.toString() == confirmPasswordEditText.text.toString()) {

                auth.createUserWithEmailAndPassword(
                    emailEditText.text.toString(),
                    passwordEditText.text.toString()
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d(CreateTAG, "createUserWithEmail:success")
                            val user = auth.currentUser
                            val newDisplayName = surnameEditText.text.toString() + " " + nameEditText.text.toString()
                            Log.d("d", newDisplayName)
                            val profileUpdates = userProfileChangeRequest {
                                displayName = newDisplayName
                            }
                            user!!.updateProfile(profileUpdates).addOnCompleteListener{ updateTask ->
                                if(updateTask.isSuccessful){
                                    Log.d(UpdateTAG, "UserUpdate:success")
                                } else Log.d(UpdateTAG, "UserUpdate:failure")
                            }
                            updateUI(user)
                        } else {
                            Log.w(CreateTAG, "createUserWithEmail:failure", it.exception)
                            Toast.makeText(baseContext, "Authentication failed", Toast.LENGTH_SHORT)
                                .show()
                            updateUI(null)
                        }
                    }
            } else {
                when {
                    passwordEditText.text.toString() == "" -> {
                        passwordEditText.setHintTextColor(Color.RED)
                    }
                    nameEditText.text.toString() == "" -> {
                        nameEditText.setHintTextColor(Color.RED)
                    }
                    surnameEditText.text.toString() == "" -> {
                        surnameEditText.setHintTextColor(Color.RED)
                    }
                    emailEditText.text.toString() == "" -> {
                        emailEditText.setHintTextColor(Color.RED)
                    }
                    else -> {
                        confirmPasswordEditText.setTextColor(Color.RED)
                        confirmPasswordEditText.setHintTextColor(Color.RED)
                    }
                }
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        //Navigate to Main activity
        if (user == null){
            Log.w(LoginTAG, "usr is null, not going to navigate")
            return
        }
        startActivity(Intent(this,  Menu::class.java))

        finish()
    }
}
